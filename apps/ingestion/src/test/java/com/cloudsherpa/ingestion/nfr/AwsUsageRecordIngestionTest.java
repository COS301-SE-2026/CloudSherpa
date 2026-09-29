package com.cloudsherpa.ingestion.nfr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.Instance;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClientBuilder;
import software.amazon.awssdk.services.cloudwatch.CloudWatchServiceClientConfiguration;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "AES_ENCRYPTION_KEY=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
      "org.jobrunr.background-job-server.enabled=false",
      "org.jobrunr.dashboard.enabled=false",
      "spring.datasource.hikari.connection-init-sql=SET search_path TO tenant_a1b6ebb6_2b13_41c2_b4ce_bc6c563ea246, public"
    })
class AwsUsageRecordIngestionTest {

  private static final Logger logger = LoggerFactory.getLogger(AwsUsageRecordIngestionTest.class);

  private static final UUID TENANT_ID = UUID.fromString("a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246");
  private static final UUID ACCOUNT_ID = UUID.fromString("06f744fd-76e5-4845-9780-ced666c26ffe");
  private static final int NUM_RECORDS_TO_SEED = 10_000;
  private static final int RECORDS_PER_SECOND_THRESHOLD = 100;
  private static final int AWS_MAX_DATAPOINTS_PER_REQUEST = 1_440;
  private static final int PERIOD_SECONDS = 60;
  private static final String RESOURCE_ID = "i-nfr-usage";
  private static final String REGION = "eu-north-1";
  private static final String TENANT_SCHEMA = "tenant_a1b6ebb6_2b13_41c2_b4ce_bc6c563ea246";

  private static final Instant FROM = Instant.parse("2026-09-01T00:00:00Z");

  @Autowired private CloudUsageService ingestionService;
  @PersistenceContext private EntityManager entityManager;

  private MockedStatic<CloudWatchClient> mockedCloudWatchClient;

  @Container @ServiceConnection
  static PostgreSQLContainer timescaledb =
      new PostgreSQLContainer(
              DockerImageName.parse("timescale/timescaledb-ha:pg16-ts2.29")
                  .asCompatibleSubstituteFor("postgres"))
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("sherpadb-schema.sql"),
              "/docker-entrypoint-initdb.d/01_schema.sql")
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("nfr-usage-user.sql"),
              "/docker-entrypoint-initdb.d/02_nfr_user.sql");

  @AfterEach
  void tearDown() {
    if (mockedCloudWatchClient != null) {
      mockedCloudWatchClient.close();
    }
  }

  @Test
  void awsUsageIngestionMeetsRecordsPerSecondThreshold() {
    CloudWatchClient client = mockCloudWatchClient();
    IngestionRequestEvent request = createRequest();

    long start = System.nanoTime();

    ingestionService.ingest(request);

    long duration = System.nanoTime() - start;
    double elapsedSeconds = duration / Math.pow(10, 9);
    double recordsPerSecond = NUM_RECORDS_TO_SEED / elapsedSeconds;

    logger.info(
        "\nDuration: {}s\n AWS usage records processed: {}\nRecords per second: {}",
        elapsedSeconds,
        NUM_RECORDS_TO_SEED,
        recordsPerSecond);

    assertEquals(NUM_RECORDS_TO_SEED, countPersistedMetrics());
    assertTrue(recordsPerSecond >= RECORDS_PER_SECOND_THRESHOLD);
    org.mockito.Mockito.verify(client, org.mockito.Mockito.times(expectedApiCalls()))
        .getMetricStatistics(any(GetMetricStatisticsRequest.class));
  }

  private CloudWatchClient mockCloudWatchClient() {
    CloudWatchClientBuilder builder = mock(CloudWatchClientBuilder.class);
    CloudWatchClient client = mock(CloudWatchClient.class);
    CloudWatchServiceClientConfiguration configuration =
        mock(CloudWatchServiceClientConfiguration.class);

    mockedCloudWatchClient = mockStatic(CloudWatchClient.class);
    mockedCloudWatchClient.when(CloudWatchClient::builder).thenReturn(builder);

    when(builder.region(any(Region.class))).thenReturn(builder);
    when(builder.credentialsProvider(any())).thenReturn(builder);
    when(builder.build()).thenReturn(client);

    when(client.serviceClientConfiguration()).thenReturn(configuration);
    when(configuration.region()).thenReturn(Region.of(REGION));

    int numberOfApiCalls = expectedApiCalls();

    final int[] responseIndex = {0};
    when(client.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
        .thenAnswer(
            invocation -> {
              int currentIndex = responseIndex[0]++;
              if (currentIndex >= numberOfApiCalls) {
                throw new IllegalStateException("AWS usage NFR made more calls than expected");
              }
              return createMetricStatisticsResponse(currentIndex);
            });

    return client;
  }

  private GetMetricStatisticsResponse createMetricStatisticsResponse(int responseIndex) {
    int firstRecordIndex = responseIndex * AWS_MAX_DATAPOINTS_PER_REQUEST;
    int recordCount =
        Math.min(AWS_MAX_DATAPOINTS_PER_REQUEST, NUM_RECORDS_TO_SEED - firstRecordIndex);

    List<Datapoint> datapoints = new ArrayList<>(recordCount);

    for (int index = 0; index < recordCount; index++) {
      int recordIndex = firstRecordIndex + index;
      Instant timestamp = FROM.plusSeconds((long) recordIndex * PERIOD_SECONDS);

      datapoints.add(
          Datapoint.builder()
              .average(50.0)
              .unit(StandardUnit.PERCENT)
              .timestamp(timestamp)
              .build());
    }

    return GetMetricStatisticsResponse.builder().datapoints(datapoints).build();
  }

  private int expectedApiCalls() {
    return (NUM_RECORDS_TO_SEED + AWS_MAX_DATAPOINTS_PER_REQUEST - 1)
        / AWS_MAX_DATAPOINTS_PER_REQUEST;
  }

  private IngestionRequestEvent createRequest() {
    Metric metric = new Metric();
    metric.setName("CPUUtilization");
    metric.setUnit("Percent");

    Instance instance = new Instance();
    instance.setIdentifier(RESOURCE_ID);
    instance.setRegion(REGION);

    InstanceScope instanceScope = new InstanceScope();
    instanceScope.setIdentifierName("InstanceId");
    instanceScope.setInstances(List.of(instance));

    ServiceScope serviceScope = new ServiceScope();
    serviceScope.setName("AWS/EC2");
    serviceScope.setMetrics(List.of(metric));
    serviceScope.setInstances(List.of(instanceScope));

    AccountScope accountScope = new AccountScope();
    accountScope.setProvider("AWS");
    accountScope.setAccountId(ACCOUNT_ID.toString());
    accountScope.setServiceScopes(List.of(serviceScope));

    CloudCredentials credentials = new CloudCredentials();
    credentials.setAccessKeyId("test-access-key");
    credentials.setSecretAccessKey("test-secret-key");

    IngestionRequestEvent request = new IngestionRequestEvent();
    request.setScopes(List.of(accountScope));
    request.setCredentials(credentials);
    request.setFrom(FROM);
    request.setTo(FROM.plusSeconds((long) NUM_RECORDS_TO_SEED * PERIOD_SECONDS));
    request.setPeriod(PERIOD_SECONDS);
    request.setIncludeUsage(true);
    request.setIncludeBilling(false);
    request.setUserId(TENANT_ID);
    return request;
  }

  private long countPersistedMetrics() {
    Object result =
        entityManager
            .createNativeQuery("SELECT COUNT(*) FROM " + TENANT_SCHEMA + ".normalized_metrics")
            .getSingleResult();
    return ((Number) result).longValue();
  }
}
