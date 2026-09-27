package com.cloudsherpa.ingestion.nfr;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.AwsCurIngestionService;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurContext;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline.AwsCurManifestStep;
import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import com.cloudsherpa.lib.entities.AwsBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.repositories.AwsBillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "AES_ENCRYPTION_KEY=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
      "org.jobrunr.background-job-server.enabled=false",
      "org.jobrunr.dashboard.enabled=false"
    })
class AwsBillingRecordIngestionTest {

  @Autowired BillingExportConfigRepository exportConfigRepository;

  @Autowired AwsBillingExportConfigRepository awsExportConfigRepository;

  @Autowired CloudCredentialRepository cloudCredentialRepository;

  @Autowired CredentialEncryptionService credentialEncryptionService;

  @Autowired BillingExportService awsExportService;

  @Autowired AwsCurIngestionService ingestionService;

  @MockitoBean AwsCurManifestStep manifestStep;

  private static final Logger logger = LoggerFactory.getLogger(AwsBillingRecordIngestionTest.class);
  private static final UUID TENANT_ID = UUID.fromString("a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246");
  private static final UUID ACCOUNT_ID = UUID.fromString("06f744fd-76e5-4845-9780-ced666c26ffe");
  private static final UUID CONFIG_ID = UUID.fromString("c181db1b-e20b-4b34-a606-af13a2d48524");
  private static final UUID CREDENTIAL_ID = UUID.fromString("94df7256-4036-4f56-bc03-74fe93158ee6");
  private static final UUID EXPORT_ID = UUID.fromString("b114912b-0c2e-45dc-932f-08fb57227d9d");
  private static final String CSV_EXPORT = "s3://test-bucket/prefix/export/data/test-export.csv.gz";
  private static final String BUCKET_NAME = "test-bucket";
  private static final String BUCKET_REGION = "eu-north-1";
  private static final String EXPORT_PREFIX = "prefix";
  private static final String EXPORT_NAME = "export";
  private static final int NUM_RECORDS_TO_SEED = 10000;
  private static final int RECORD_PER_SECOND_THRESHOLD = 1000;

  private MockedStatic<S3Client> mockedS3Client;

  @Container @ServiceConnection
  static PostgreSQLContainer timescaledb =
      new PostgreSQLContainer(
              DockerImageName.parse("timescale/timescaledb-ha:pg16-ts2.29")
                  .asCompatibleSubstituteFor("postgres"))
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("sherpadb-schema.sql"),
              "/docker-entrypoint-initdb.d/01_schema.sql")
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("nfr-user.sql"),
              "/docker-entrypoint-initdb.d/02_nfr_user.sql");

  private void persistConfigs() {
    BillingExportConfig config =
        new BillingExportConfig(
            CONFIG_ID, ACCOUNT_ID, OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));
    exportConfigRepository.save(config);

    AwsBillingExportConfig awsConfig =
        new AwsBillingExportConfig(
            CONFIG_ID, BUCKET_NAME, BUCKET_REGION, EXPORT_PREFIX, EXPORT_NAME);
    awsExportConfigRepository.save(awsConfig);
  }

  private void persistCredentials() {
    String credentialJson =
        """
            {
              "accessKeyId": "test-access-key",
              "secretAccessKey": "test-secret-key"
            }
            """;

    CloudCredential credential =
        new CloudCredential(
            CREDENTIAL_ID,
            ACCOUNT_ID,
            "AWS",
            "access_key",
            credentialEncryptionService.encrypt(credentialJson),
            OffsetDateTime.now());
    cloudCredentialRepository.save(credential);
  }

  private void mockManifestStep() {
    doAnswer(
            invocation -> {
              AwsCurContext context = invocation.getArgument(0);
              BillingExport export =
                  awsExportService.initializeExport(
                      EXPORT_ID.toString(), context.getConfigId(), List.of(CSV_EXPORT));
              export.setEncoding("CSV");
              context.getProcessingExports().add(export);
              return null;
            })
        .when(manifestStep)
        .execute(any(AwsCurContext.class));
  }

  private byte[] createCsvExport(int recordCount) {
    StringBuilder csv =
        new StringBuilder(
            "line_item_usage_account_id,line_item_resource_id,line_item_line_item_type,"
                + "product_servicecode,line_item_unblended_cost,line_item_usage_start_date,"
                + "line_item_usage_end_date,product_sku\n");

    for (int index = 0; index < recordCount; index++) {
      csv.append("123456789012,arn:aws:ec2:eu-north-1:123456789012:instance/i-")
          .append(index)
          .append(",Usage,AmazonEC2,0.01,2026-09-01T00:00:00Z,")
          .append("2026-09-01T01:00:00Z,sku-")
          .append(index)
          .append('\n');
    }

    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
      gzip.write(csv.toString().getBytes(StandardCharsets.UTF_8));
      gzip.finish();
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new UncheckedIOException("Failed to construct mock AWS CUR CSV", exception);
    }
  }

  private void mockCsvResponseInputStream(int recordCount) {
    byte[] csvBytes = createCsvExport(recordCount);
    S3ClientBuilder builder = mock(S3ClientBuilder.class);
    S3Client s3Client = mock(S3Client.class);
    S3Utilities utilities = mock(S3Utilities.class);
    S3Uri parsedUri = mock(S3Uri.class);

    mockedS3Client = mockStatic(S3Client.class);
    mockedS3Client.when(S3Client::builder).thenReturn(builder);
    when(builder.region(any(Region.class))).thenReturn(builder);
    when(builder.credentialsProvider(any())).thenReturn(builder);
    when(builder.build()).thenReturn(s3Client);
    when(s3Client.utilities()).thenReturn(utilities);
    when(utilities.parseUri(any(URI.class))).thenReturn(parsedUri);
    when(parsedUri.bucket()).thenReturn(Optional.of(BUCKET_NAME));
    when(parsedUri.key()).thenReturn(Optional.of("prefix/export/data/test-export.csv.gz"));
    when(s3Client.getObject(any(GetObjectRequest.class)))
        .thenAnswer(
            invocation ->
                new ResponseInputStream<>(
                    GetObjectResponse.builder().build(),
                    AbortableInputStream.create(new ByteArrayInputStream(csvBytes))));
  }

  @BeforeEach
  void setUp() {
    persistConfigs();
    persistCredentials();
    mockManifestStep();
    mockCsvResponseInputStream(NUM_RECORDS_TO_SEED);
  }

  @AfterEach
  void tearDown() {
    mockedS3Client.close();
  }

  @Test
  void awsCurIngestionMeetsRecordsPerSecondThreshold() {

    long start = System.nanoTime();

    ingestionService.execute(TENANT_ID.toString(), CONFIG_ID.toString());

    long duration = System.nanoTime() - start;
    double elapsedSeconds = duration / Math.pow(10, 9);

    double recordsPerSecond = NUM_RECORDS_TO_SEED / elapsedSeconds;
    logger.info(
        "\nDuration: {}s\nRecords processed: {}\nRecords per second: {}",
        elapsedSeconds,
        NUM_RECORDS_TO_SEED,
        recordsPerSecond);

    assertTrue(recordsPerSecond > RECORD_PER_SECOND_THRESHOLD);
  }
}
