package com.cloudsherpa.ingestion.unit.provider.azure.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.azure.factory.AzureClientFactory;
import com.cloudsherpa.ingestion.provider.azure.monitoring.AzureCloudMonitorMetricProvider;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AzureCloudMonitorMetricProviderTest {

  private AzureCloudMonitorMetricProvider provider;

  @BeforeEach
  void setUp() {
    provider = new AzureCloudMonitorMetricProvider();
  }

  // Validation tests

  @Test
  void collectMetrics_shouldThrow_whenAccountScopeIsNull() {
    IngestionRequestEvent request = validRequest();

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(null, request));

    assertEquals("Account scope cannot be null", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenRequestIsNull() {
    AccountScope scope = validScope();

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, null));

    assertEquals("Ingestion request cannot be null", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenCredentialsMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();
    request.setCredentials(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Azure credentials are required for Azure metric ingestion", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenSubscriptionMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.getCredentials().setSubscriptionId(" ");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Azure subscriptionId is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenTenantMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.getCredentials().setTenantId(" ");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Azure tenantId is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenClientIdMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.getCredentials().setClientId(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Azure clientId is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenClientSecretMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.getCredentials().setClientSecret(" ");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Azure clientSecret is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenAccountIdMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    scope.setAccountId(" ");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("AccountId is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenFromMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setFrom(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Metric request from/to timestamps are required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenToMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setTo(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Metric request from/to timestamps are required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenFromAfterTo() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setFrom(Instant.parse("2026-01-01T02:00:00Z"));
    request.setTo(Instant.parse("2026-01-01T01:00:00Z"));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Metric request 'from' must be before 'to'", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenFromEqualsTo() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    Instant timestamp = Instant.parse("2026-01-01T01:00:00Z");

    request.setFrom(timestamp);
    request.setTo(timestamp);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Metric request 'from' must be before 'to'", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenPeriodIsZero() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setPeriod(0);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Metric period must be > 0", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenPeriodIsNegative() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setPeriod(-1);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertEquals("Metric period must be > 0", ex.getMessage());
  }

  // Helper functions

  private List<UsageRecordModel> collectSingleMetricWithValueSetup(
      java.util.function.Consumer<MetricValue> valueSetup) {

    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    MetricResult metricResult = mock(MetricResult.class);

    TimeSeriesElement series = mock(TimeSeriesElement.class);

    MetricValue value = mock(MetricValue.class);

    OffsetDateTime timestamp = OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(value.getTimeStamp()).thenReturn(timestamp);

    valueSetup.accept(value);

    when(series.getValues()).thenReturn(List.of(value));

    when(series.getMetadata()).thenReturn(Map.of());

    when(metricResult.getMetricName()).thenReturn("Percentage CPU");

    when(metricResult.getResourceType()).thenReturn("Microsoft.Compute/virtualMachines");

    when(metricResult.getTimeSeries()).thenReturn(List.of(series));

    when(resourceResult.getResourceId())
        .thenReturn(
            "/subscriptions/sub/resourceGroups/rg/"
                + "providers/Microsoft.Compute/"
                + "virtualMachines/vm-01");

    when(resourceResult.getMetrics()).thenReturn(List.of(metricResult));

    when(result.getMetricsQueryResults()).thenReturn(List.of(resourceResult));

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      return provider.collectMetrics(scope, request);
    }
  }

  private AccountScope validScope() {
    Metric metric = new Metric();

    metric.setName("Percentage CPU");
    metric.setUnit("Percent");

    Instance instance = new Instance();

    instance.setIdentifier(
        "/subscriptions/sub/resourceGroups/rg/"
            + "providers/Microsoft.Compute/"
            + "virtualMachines/vm-01");

    instance.setRegion("westeurope");

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setIdentifierName("resourceId");

    instanceScope.setInstances(List.of(instance));

    ServiceScope service = new ServiceScope();

    service.setName("Microsoft.Compute/virtualMachines");

    service.setMetrics(List.of(metric));

    service.setInstances(List.of(instanceScope));

    AccountScope scope = new AccountScope();

    scope.setProvider("AZURE");

    scope.setAccountId("subscription-123");

    scope.setSubscriptionId("subscription-123");

    scope.setServiceScopes(List.of(service));

    return scope;
  }

  private IngestionRequestEvent validRequest() {
    CloudCredentials credentials = new CloudCredentials();

    credentials.setSubscriptionId("subscription-123");

    credentials.setTenantId("tenant-123");

    credentials.setClientId("client-123");

    credentials.setClientSecret("secret-123");

    IngestionRequestEvent request = new IngestionRequestEvent();

    request.setCredentials(credentials);

    request.setFrom(Instant.parse("2026-01-01T00:00:00Z"));

    request.setTo(Instant.parse("2026-01-01T01:00:00Z"));

    request.setPeriod(300);

    return request;
  }
}
