package com.cloudsherpa.ingestion.nfr;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.provider.azure.AzureBillingIngestionService;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline.ManifestDiscoveryStep;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline.ManifestParsingStep;
import com.cloudsherpa.ingestion.provider.azure.services.blobstorage.AzureBlobReader;
import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.repositories.AzureBillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "AES_ENCRYPTION_KEY=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
      "org.jobrunr.background-job-server.enabled=false",
      "org.jobrunr.dashboard.enabled=false"
    })
class AzureBillingRecordIngestionTest {

  private static final UUID TENANT_ID = UUID.fromString("a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246");
  private static final UUID ACCOUNT_ID = UUID.fromString("a0000000-0000-0000-0000-000000000003");
  private static final UUID CONFIG_ID = UUID.fromString("c181db1b-e20b-4b34-a606-af13a2d48524");
  private static final UUID CREDENTIAL_ID = UUID.fromString("94df7256-4036-4f56-bc03-74fe93158ee7");
  private static final String STORAGE_ACCOUNT_NAME = "nfrstorageaccount";
  private static final String STORAGE_CONTAINER = "billing-exports";
  private static final String BILLING_EXPORT_DIRECTORY = "exports/daily";
  private static final String BILLING_EXPORT_NAME = "nfr-billing-export";
  private static final String RUN_ID = "nfr-run";
  private static final String BLOB_NAME = "exports/daily/nfr-billing-export/part0.csv.gz";
  private static final int NUM_RECORDS_TO_SEED = 10000;
  private static final int RECORD_PER_SECOND_THRESHOLD = 1000;
  private static final Logger logger =
      LoggerFactory.getLogger(AzureBillingRecordIngestionTest.class);

  @Autowired BillingExportConfigRepository exportConfigRepository;
  @Autowired AzureBillingExportConfigRepository azureExportConfigRepository;
  @Autowired CloudCredentialRepository cloudCredentialRepository;
  @Autowired CredentialEncryptionService credentialEncryptionService;
  @Autowired AzureBillingIngestionService ingestionService;
  @MockitoBean ManifestDiscoveryStep manifestDiscoveryStep;
  @MockitoBean ManifestParsingStep manifestParsingStep;
  @MockitoBean AzureBlobReader blobReader;

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

  @BeforeEach
  void setUp() {
    persistConfigs();
    writeCredentials();
    mockManifestDiscoveryStep();
    mockManifestParsingStep();
    mockCsvInputStream(NUM_RECORDS_TO_SEED);
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

  private void mockManifestDiscoveryStep() {
    doAnswer(
            invocation -> {
              AzureBillingContext context = invocation.getArgument(0);
              context.setBlobContainerClient(mock(BlobContainerClient.class));
              return null;
            })
        .when(manifestDiscoveryStep)
        .execute(any(AzureBillingContext.class));
  }

  private void mockManifestParsingStep() {
    AzureManifest manifest =
        new AzureManifest(
            100,
            1,
            NUM_RECORDS_TO_SEED,
            new AzureManifest.RunInfo(
                Instant.parse("2026-09-01T00:30:00Z"),
                RUN_ID,
                Instant.parse("2026-08-01T00:00:00Z"),
                Instant.parse("2026-09-01T00:00:00Z")),
            new AzureManifest.DeliveryConfig("Csv"),
            List.of(new AzureManifest.Partition(BLOB_NAME, 100, NUM_RECORDS_TO_SEED)));
    UUID executionId =
        UUID.nameUUIDFromBytes((CONFIG_ID + ":" + RUN_ID).getBytes(StandardCharsets.UTF_8));

    doAnswer(
            invocation -> {
              AzureBillingContext context = invocation.getArgument(0);
              context.setManifests(Map.of(executionId, manifest));
              return null;
            })
        .when(manifestParsingStep)
        .execute(any(AzureBillingContext.class));
  }

  private void mockCsvInputStream(int recordCount) {
    byte[] csvBytes = createCsvExport(recordCount);

    when(blobReader.openStream(any(BlobContainerClient.class), eq(BLOB_NAME)))
        .thenAnswer(invocation -> new ByteArrayInputStream(csvBytes));
  }

  private byte[] createCsvExport(int recordCount) {
    StringBuilder csv =
        new StringBuilder(
            "billingAccountId,date,consumedService,meterCategory,meterSubCategory,"
                + "ResourceId,chargeType,billingCurrency,costInPricingCurrency\n");

    for (int index = 0; index < recordCount; index++) {
      csv.append("billing-account,09/01/2026,Microsoft.Compute,Virtual Machines,")
          .append("Dv3 Series,/subscriptions/test/resourceGroups/nfr/providers/")
          .append("Microsoft.Compute/virtualMachines/vm-")
          .append(index)
          .append(",Usage,USD,0.01\n");
    }

    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
      gzip.write(csv.toString().getBytes(StandardCharsets.UTF_8));
      gzip.finish();
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new UncheckedIOException("Failed to construct mock Azure billing CSV", exception);
    }
  }

  private void persistConfigs() {
    BillingExportConfig config =
        new BillingExportConfig(
            CONFIG_ID, ACCOUNT_ID, OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));
    exportConfigRepository.save(config);

    AzureBillingExportConfig azureConfig =
        new AzureBillingExportConfig(
            CONFIG_ID,
            STORAGE_ACCOUNT_NAME,
            STORAGE_CONTAINER,
            BILLING_EXPORT_DIRECTORY,
            BILLING_EXPORT_NAME);
    azureExportConfigRepository.save(azureConfig);
  }

  private void writeCredentials() {
    String credentialJson =
        """
            {
              "subscriptionId": "00000000-0000-0000-0000-000000000010",
              "tenantId": "00000000-0000-0000-0000-000000000011",
              "clientId": "00000000-0000-0000-0000-000000000012",
              "clientSecret": "test-client-secret"
            }
            """;

    CloudCredential credential =
        new CloudCredential(
            CREDENTIAL_ID,
            ACCOUNT_ID,
            "AZURE",
            "oauth",
            credentialEncryptionService.encrypt(credentialJson),
            OffsetDateTime.now());
    cloudCredentialRepository.save(credential);
  }
}
