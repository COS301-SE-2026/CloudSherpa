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
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.util.Timestamps;
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
class GcpUsageRecordIngestionTest {

  private static final Logger logger = LoggerFactory.getLogger(GcpUsageRecordIngestionTest.class);

  private static final UUID TENANT_ID = UUID.fromString("a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246");
  private static final UUID ACCOUNT_ID = UUID.fromString("a0000000-0000-0000-0000-000000000002");
  private static final int NUM_RECORDS_TO_SEED = 10_000;
  private static final int RECORDS_PER_SECOND_THRESHOLD = 100;
  private static final int PERIOD_SECONDS = 60;
  private static final String PROJECT_ID = "nfr-project";
  private static final String RESOURCE_ID = "1234567890123456789";
  private static final String REGION = "europe-west1-b";
  private static final String TENANT_SCHEMA = "tenant_a1b6ebb6_2b13_41c2_b4ce_bc6c563ea246";

  private static final Instant FROM = Instant.parse("2026-09-01T00:00:00Z");

  @Autowired private CloudUsageService ingestionService;
  @PersistenceContext private EntityManager entityManager;

  private MockedStatic<MetricServiceClient> mockedMetricServiceClient;

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
    if (mockedMetricServiceClient != null) {
      mockedMetricServiceClient.close();
    }
  }

  @Test
  void gcpUsageIngestionMeetsRecordsPerSecondThreshold() {
    MetricServiceClient client = mockMetricServiceClient();
    IngestionRequestEvent request = createRequest();

    long start = System.nanoTime();

    ingestionService.ingest(request);

    long duration = System.nanoTime() - start;
    double elapsedSeconds = duration / Math.pow(10, 9);
    double recordsPerSecond = NUM_RECORDS_TO_SEED / elapsedSeconds;

    logger.info(
        "\nDuration: {}s\nGcp usage records processed: {}\nRecords per second: {}",
        elapsedSeconds,
        NUM_RECORDS_TO_SEED,
        recordsPerSecond);

    assertEquals(NUM_RECORDS_TO_SEED, countPersistedMetrics());
    assertTrue(recordsPerSecond >= RECORDS_PER_SECOND_THRESHOLD);
    org.mockito.Mockito.verify(client).listTimeSeries(any());
  }

  private MetricServiceClient mockMetricServiceClient() {
    MetricServiceClient client = mock(MetricServiceClient.class);
    MetricServiceClient.ListTimeSeriesPagedResponse response =
        mock(MetricServiceClient.ListTimeSeriesPagedResponse.class);
    TimeSeries series = createTimeSeries();

    when(response.iterateAll()).thenReturn(List.of(series));
    when(client.listTimeSeries(any())).thenReturn(response);

    mockedMetricServiceClient = mockStatic(MetricServiceClient.class);
    mockedMetricServiceClient
        .when(() -> MetricServiceClient.create(any(MetricServiceSettings.class)))
        .thenReturn(client);

    return client;
  }

  private TimeSeries createTimeSeries() {
    TimeSeries.Builder series = TimeSeries.newBuilder();
    List<Point> points = new ArrayList<>(NUM_RECORDS_TO_SEED);

    for (int index = 0; index < NUM_RECORDS_TO_SEED; index++) {
      Instant end = FROM.plusSeconds((long) index * PERIOD_SECONDS);
      Instant start = end.minusSeconds(PERIOD_SECONDS);

      points.add(
          Point.newBuilder()
              .setInterval(
                  TimeInterval.newBuilder()
                      .setStartTime(Timestamps.fromMillis(start.toEpochMilli()))
                      .setEndTime(Timestamps.fromMillis(end.toEpochMilli()))
                      .build())
              .setValue(TypedValue.newBuilder().setDoubleValue(50.0).build())
              .build());
    }

    series.addAllPoints(points);
    return series.build();
  }

  private IngestionRequestEvent createRequest() {
    Metric metric = new Metric();
    metric.setName("compute.googleapis.com/instance/cpu/utilization");
    metric.setUnit("1");

    Instance instance = new Instance();
    instance.setIdentifier(RESOURCE_ID);
    instance.setRegion(REGION);

    InstanceScope instanceScope = new InstanceScope();
    instanceScope.setIdentifierName("instance_id");
    instanceScope.setInstances(List.of(instance));

    ServiceScope serviceScope = new ServiceScope();
    serviceScope.setName("gce_instance");
    serviceScope.setMetrics(List.of(metric));
    serviceScope.setInstances(List.of(instanceScope));

    AccountScope accountScope = new AccountScope();
    accountScope.setProvider("GCP");
    accountScope.setAccountId(ACCOUNT_ID.toString());
    accountScope.setProjectId(PROJECT_ID);
    accountScope.setServiceScopes(List.of(serviceScope));

    CloudCredentials credentials = new CloudCredentials();
    credentials.setProjectId(PROJECT_ID);
    credentials.setServiceAccountJson(
        """
            {
              "type": "authorized_user",
              "client_id": "nfr-test-client.apps.googleusercontent.com",
              "client_secret": "nfr-test-secret",
              "refresh_token": "nfr-test-refresh-token"
            }
            """);

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
