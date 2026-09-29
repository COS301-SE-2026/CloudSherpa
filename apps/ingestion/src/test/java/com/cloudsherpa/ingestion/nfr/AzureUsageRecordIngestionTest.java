package com.cloudsherpa.ingestion.nfr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.azure.core.http.rest.Response;
import com.azure.monitor.query.metrics.MetricsClient;
import com.azure.monitor.query.metrics.models.MetricResult;
import com.azure.monitor.query.metrics.models.MetricValue;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesResult;
import com.azure.monitor.query.metrics.models.MetricsQueryResult;
import com.azure.monitor.query.metrics.models.TimeSeriesElement;
import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.Instance;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.provider.azure.factory.AzureClientFactory;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
class AzureUsageRecordIngestionTest {

  private static final Logger logger = LoggerFactory.getLogger(AzureUsageRecordIngestionTest.class);

  private static final UUID TENANT_ID = UUID.fromString("a1b6ebb6-2b13-41c2-b4ce-bc6c563ea246");
  private static final UUID ACCOUNT_ID = UUID.fromString("a0000000-0000-0000-0000-000000000003");
  private static final int NUM_RECORDS_TO_SEED = 10_000;
  private static final int RECORDS_PER_SECOND_THRESHOLD = 100;
  private static final int PERIOD_SECONDS = 60;
  private static final String REGION = "westeurope";
  private static final String RESOURCE_ID =
      "/subscriptions/00000000-0000-0000-0000-000000000003/"
          + "resourceGroups/nfr/providers/Microsoft.Compute/virtualMachines/nfr-vm";
  private static final String TENANT_SCHEMA = "tenant_a1b6ebb6_2b13_41c2_b4ce_bc6c563ea246";

  private static final Instant FROM = Instant.parse("2026-09-01T00:00:00Z");

  @Autowired private CloudUsageService ingestionService;
  @PersistenceContext private EntityManager entityManager;

  private MockedStatic<AzureClientFactory> mockedAzureClientFactory;

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
    if (mockedAzureClientFactory != null) {
      mockedAzureClientFactory.close();
    }
  }

  @Test
  void azureUsageIngestionMeetsRecordsPerSecondThreshold() {
    MetricsClient client = mockMetricsClient();
    IngestionRequestEvent request = createRequest();

    long start = System.nanoTime();

    ingestionService.ingest(request);

    long duration = System.nanoTime() - start;
    double elapsedSeconds = duration / Math.pow(10, 9);
    double recordsPerSecond = NUM_RECORDS_TO_SEED / elapsedSeconds;

    logger.info(
        "\nDuration: {}s\nAzure usage records processed: {}\nRecords per second: {}",
        elapsedSeconds,
        NUM_RECORDS_TO_SEED,
        recordsPerSecond);

    assertEquals(NUM_RECORDS_TO_SEED, countPersistedMetrics());
    assertTrue(recordsPerSecond >= RECORDS_PER_SECOND_THRESHOLD);
    org.mockito.Mockito.verify(client, org.mockito.Mockito.times(1))
        .queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
  }

  private MetricsClient mockMetricsClient() {
    MetricsClient client = mock(MetricsClient.class);
    Response<MetricsQueryResourcesResult> response = mock(Response.class);
    MetricsQueryResourcesResult resourcesResult = mock(MetricsQueryResourcesResult.class);
    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);
    MetricResult metricResult = mock(MetricResult.class);
    TimeSeriesElement timeSeries = mock(TimeSeriesElement.class);

    List<MetricValue> values = createMetricValues();

    when(response.getValue()).thenReturn(resourcesResult);
    when(resourcesResult.getMetricsQueryResults()).thenReturn(List.of(resourceResult));
    when(resourceResult.getResourceId()).thenReturn(RESOURCE_ID);
    when(resourceResult.getMetrics()).thenReturn(List.of(metricResult));
    when(metricResult.getMetricName()).thenReturn("Percentage CPU");
    when(metricResult.getTimeSeries()).thenReturn(List.of(timeSeries));
    when(timeSeries.getValues()).thenReturn(values);

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    mockedAzureClientFactory = mockStatic(AzureClientFactory.class);
    mockedAzureClientFactory
        .when(() -> AzureClientFactory.createMetricsClient(any(), anyString()))
        .thenReturn(client);

    return client;
  }

  private List<MetricValue> createMetricValues() {
    List<MetricValue> values = new ArrayList<>(NUM_RECORDS_TO_SEED);

    for (int index = 0; index < NUM_RECORDS_TO_SEED; index++) {
      MetricValue value = mock(MetricValue.class);
      OffsetDateTime timestamp =
          FROM.plusSeconds((long) index * PERIOD_SECONDS).atOffset(ZoneOffset.UTC);

      when(value.getTimeStamp()).thenReturn(timestamp);
      when(value.getAverage()).thenReturn(50.0);

      values.add(value);
    }

    return values;
  }

  private IngestionRequestEvent createRequest() {
    Metric metric = new Metric();
    metric.setName("Percentage CPU");
    metric.setUnit("Percent");

    Instance instance = new Instance();
    instance.setIdentifier(RESOURCE_ID);
    instance.setRegion(REGION);

    InstanceScope instanceScope = new InstanceScope();
    instanceScope.setIdentifierName("resourceId");
    instanceScope.setInstances(List.of(instance));

    ServiceScope serviceScope = new ServiceScope();
    serviceScope.setName("Microsoft.Compute/virtualMachines");
    serviceScope.setMetrics(List.of(metric));
    serviceScope.setInstances(List.of(instanceScope));

    AccountScope accountScope = new AccountScope();
    accountScope.setProvider("AZURE");
    accountScope.setAccountId(ACCOUNT_ID.toString());
    accountScope.setSubscriptionId(ACCOUNT_ID.toString());
    accountScope.setServiceScopes(List.of(serviceScope));

    CloudCredentials credentials = new CloudCredentials();
    credentials.setSubscriptionId(ACCOUNT_ID.toString());
    credentials.setTenantId("00000000-0000-0000-0000-000000000011");
    credentials.setClientId("00000000-0000-0000-0000-000000000012");
    credentials.setClientSecret("test-client-secret");

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
