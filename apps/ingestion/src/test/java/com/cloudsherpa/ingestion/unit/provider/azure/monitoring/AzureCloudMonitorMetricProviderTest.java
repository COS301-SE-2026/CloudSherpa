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
import java.util.ArrayList;
import java.util.Collections;
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

  // Metric collection tests

  @Test
  void collectMetrics_shouldMapAzureResponseIntoUsageRecord() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    MetricResult metricResult = mock(MetricResult.class);

    TimeSeriesElement series = mock(TimeSeriesElement.class);

    MetricValue value = mock(MetricValue.class);

    OffsetDateTime timestamp = OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    when(response.getValue()).thenReturn(result);

    when(value.getTimeStamp()).thenReturn(timestamp);
    when(value.getAverage()).thenReturn(73.5);

    when(series.getValues()).thenReturn(List.of(value));

    when(series.getMetadata()).thenReturn(Map.of("VMName", "vm-01", "Environment", "dev"));

    when(metricResult.getMetricName()).thenReturn("Percentage CPU");

    when(metricResult.getTimeSeries()).thenReturn(List.of(series));

    when(metricResult.getResourceType()).thenReturn("Microsoft.Compute/virtualMachines");

    /*
     * Deliberately leave Azure's unit null.
     *
     * The unit fallback behaviour is intentional and is
     * explicitly not tested here. Azure ingestion returns a unit
     * and therefore no fallback unit is necessary. GCP does not return this
     * unit and the fallback in the request is intended for GCP only
     */
    when(metricResult.getUnit()).thenReturn(null);

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
          .when(
              () ->
                  AzureClientFactory.createMetricsClient(
                      any(CloudCredentials.class), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertEquals(1, usages.size());

      UsageRecordModel usage = usages.get(0);

      assertEquals("AZURE", usage.getProvider());
      assertEquals("subscription-123", usage.getAccountId());
      assertEquals("subscription-123", usage.getSubscriptionId());

      assertEquals("Microsoft.Compute/virtualMachines", usage.getServiceName());

      assertEquals("Percentage CPU", usage.getMetricName());

      assertEquals(
          "/subscriptions/sub/resourceGroups/rg/"
              + "providers/Microsoft.Compute/"
              + "virtualMachines/vm-01",
          usage.getResourceId());

      assertEquals("Microsoft.Compute/virtualMachines", usage.getResourceType());

      assertEquals("westeurope", usage.getRegion());

      assertNull(usage.getUnit());

      assertEquals(73.5, usage.getValue());

      assertEquals(timestamp.toInstant(), usage.getTimestamp());

      assertEquals(timestamp.toInstant(), usage.getPeriodStart());

      assertEquals(timestamp.toInstant().plusSeconds(300), usage.getPeriodEnd());

      assertEquals("AzureMonitor", usage.getSource());

      assertEquals(Map.of("VMName", "vm-01", "Environment", "dev"), usage.getDimensions());

      assertNotNull(usage.getRecordId());
      assertNotNull(usage.getIngestionId());
      assertNotNull(usage.getIngestionTimestamp());
    }
  }

  // Region grouping

  @Test
  void collectMetrics_shouldCreateSeparateClientPerRegion() {
    AccountScope scope = validScope();

    Instance first = scope.getServiceScopes().get(0).getInstances().get(0).getInstances().get(0);

    Instance second = new Instance();

    second.setIdentifier(
        "/subscriptions/sub/resourceGroups/rg/"
            + "providers/Microsoft.Compute/"
            + "virtualMachines/vm-02");

    second.setRegion("eastus");

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setInstances(List.of(first, second));

    scope.getServiceScopes().get(0).setInstances(List.of(instanceScope));

    IngestionRequestEvent request = validRequest();

    MetricsClient west = mock(MetricsClient.class);

    MetricsClient east = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);
    when(result.getMetricsQueryResults()).thenReturn(List.of());

    when(west.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    when(east.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(west);

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("eastus")))
          .thenReturn(east);

      provider.collectMetrics(scope, request);

      verify(west, times(1))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());

      verify(east, times(1))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  // Resource batching tests

  @Test
  void collectMetrics_shouldUseOneRequestForExactly50Resources() {
    AccountScope scope = validScope();

    List<Instance> instances = new ArrayList<>();

    for (int i = 0; i < 50; i++) {
      Instance instance = new Instance();

      instance.setIdentifier(
          "/subscriptions/sub/resourceGroups/rg/"
              + "providers/Microsoft.Compute/"
              + "virtualMachines/vm-"
              + i);

      instance.setRegion("westeurope");

      instances.add(instance);
    }

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setInstances(instances);

    scope.getServiceScopes().get(0).setInstances(List.of(instanceScope));

    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);
    when(result.getMetricsQueryResults()).thenReturn(List.of());

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request);

      verify(client, times(1))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldBatchMoreThan50Resources() {
    AccountScope scope = validScope();

    List<Instance> instances = new ArrayList<>();

    for (int i = 0; i < 51; i++) {
      Instance instance = new Instance();

      instance.setIdentifier(
          "/subscriptions/sub/resourceGroups/rg/"
              + "providers/Microsoft.Compute/"
              + "virtualMachines/vm-"
              + i);

      instance.setRegion("westeurope");

      instances.add(instance);
    }

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setInstances(instances);

    scope.getServiceScopes().get(0).setInstances(List.of(instanceScope));

    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);
    when(result.getMetricsQueryResults()).thenReturn(List.of());

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request);

      verify(client, times(2))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  // Metric batching

  @Test
  void collectMetrics_shouldUseOneRequestFor20Metrics() {
    AccountScope scope = validScope();

    List<Metric> metrics = new ArrayList<>();

    for (int i = 0; i < 20; i++) {
      Metric metric = new Metric();

      metric.setName("Metric-" + i);
      metrics.add(metric);
    }

    scope.getServiceScopes().get(0).setMetrics(metrics);

    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);
    when(result.getMetricsQueryResults()).thenReturn(List.of());

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request);

      verify(client, times(1))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldBatchMoreThan20Metrics() {
    AccountScope scope = validScope();

    List<Metric> metrics = new ArrayList<>();

    for (int i = 0; i < 21; i++) {
      Metric metric = new Metric();

      metric.setName("Metric-" + i);
      metrics.add(metric);
    }

    scope.getServiceScopes().get(0).setMetrics(metrics);

    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);
    when(result.getMetricsQueryResults()).thenReturn(List.of());

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request);

      verify(client, times(2))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  // Invalid requests

  @Test
  void collectMetrics_shouldIgnoreInstancesWithoutIdentifier() {
    AccountScope scope = validScope();

    Instance invalid = new Instance();

    invalid.setIdentifier(null);
    invalid.setRegion("westeurope");

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setInstances(List.of(invalid));

    scope.getServiceScopes().get(0).setInstances(List.of(instanceScope));

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldIgnoreBlankInstanceIdentifier() {
    AccountScope scope = validScope();

    Instance invalid = new Instance();

    invalid.setIdentifier(" ");
    invalid.setRegion("westeurope");

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setInstances(List.of(invalid));

    scope.getServiceScopes().get(0).setInstances(List.of(instanceScope));

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldThrowWhenRegionMissing() {
    AccountScope scope = validScope();

    scope.getServiceScopes().get(0).getInstances().get(0).getInstances().get(0).setRegion(" ");

    IngestionRequestEvent request = validRequest();

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> provider.collectMetrics(scope, request));

    assertTrue(ex.getMessage().contains("Azure resource is missing its region"));
  }

  // Null/empty service or instance scope, or empty metric lists

  @Test
  void collectMetrics_shouldSkipNullServiceScope() {
    AccountScope scope = validScope();

    scope.setServiceScopes(Collections.singletonList(null));

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldSkipServiceWithoutInstances() {
    AccountScope scope = validScope();

    scope.getServiceScopes().get(0).setInstances(List.of());

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldSkipServiceWithNullInstances() {
    AccountScope scope = validScope();

    scope.getServiceScopes().get(0).setInstances(null);

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldSkipServiceWithoutMetrics() {
    AccountScope scope = validScope();

    scope.getServiceScopes().get(0).setMetrics(List.of());

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldSkipServiceWithNullMetrics() {
    AccountScope scope = validScope();

    scope.getServiceScopes().get(0).setMetrics(null);

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldSkipServiceWithBlankName() {
    AccountScope scope = validScope();

    scope.getServiceScopes().get(0).setName(" ");

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldSkipServiceWithNullName() {
    AccountScope scope = validScope();

    scope.getServiceScopes().get(0).setName(null);

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldIgnoreNullInstanceScope() {
    AccountScope scope = validScope();

    scope.getServiceScopes().get(0).setInstances(Collections.singletonList(null));

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  @Test
  void collectMetrics_shouldIgnoreInstanceScopeWithNullInstances() {
    AccountScope scope = validScope();

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setInstances(null);

    scope.getServiceScopes().get(0).setInstances(List.of(instanceScope));

    IngestionRequestEvent request = validRequest();

    List<UsageRecordModel> result = provider.collectMetrics(scope, request);

    assertTrue(result.isEmpty());
  }

  // Empty/malformed Azure responses or rejections of sent request

  @Test
  void collectMetrics_shouldReturnEmptyListWhenAzureResponseValueIsNull() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(null);

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> result = provider.collectMetrics(scope, request);

      assertTrue(result.isEmpty());
    }
  }

  @Test
  void collectMetrics_shouldReturnEmptyListWhenMetricsQueryResultsIsNull() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(null);

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreNullResourceResult() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(Collections.singletonList(null));

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreResourceResultWithoutResourceId() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(List.of(resourceResult));

    when(resourceResult.getResourceId()).thenReturn(null);

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreUnexpectedResourceReturnedByAzure() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(List.of(resourceResult));

    when(resourceResult.getResourceId())
        .thenReturn(
            "/subscriptions/sub/resourceGroups/rg/"
                + "providers/Microsoft.Compute/"
                + "virtualMachines/unexpected");

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreResourceResultWithNullMetrics() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(List.of(resourceResult));

    when(resourceResult.getResourceId())
        .thenReturn(
            "/subscriptions/sub/resourceGroups/rg/"
                + "providers/Microsoft.Compute/"
                + "virtualMachines/vm-01");

    when(resourceResult.getMetrics()).thenReturn(null);

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  // Metrics and time-series handling

  @Test
  void collectMetrics_shouldIgnoreNullMetricResult() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(List.of(resourceResult));

    when(resourceResult.getResourceId())
        .thenReturn(
            "/subscriptions/sub/resourceGroups/rg/"
                + "providers/Microsoft.Compute/"
                + "virtualMachines/vm-01");

    when(resourceResult.getMetrics()).thenReturn(Collections.singletonList(null));

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreMetricResultWithNullTimeSeries() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    MetricResult metricResult = mock(MetricResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(List.of(resourceResult));

    when(resourceResult.getResourceId())
        .thenReturn(
            "/subscriptions/sub/resourceGroups/rg/"
                + "providers/Microsoft.Compute/"
                + "virtualMachines/vm-01");

    when(resourceResult.getMetrics()).thenReturn(List.of(metricResult));

    when(metricResult.getTimeSeries()).thenReturn(null);

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreNullTimeSeries() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    MetricResult metricResult = mock(MetricResult.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(List.of(resourceResult));

    when(resourceResult.getResourceId())
        .thenReturn(
            "/subscriptions/sub/resourceGroups/rg/"
                + "providers/Microsoft.Compute/"
                + "virtualMachines/vm-01");

    when(resourceResult.getMetrics()).thenReturn(List.of(metricResult));

    when(metricResult.getTimeSeries()).thenReturn(Collections.singletonList(null));

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreTimeSeriesWithNullValues() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    MetricResult metricResult = mock(MetricResult.class);

    TimeSeriesElement series = mock(TimeSeriesElement.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(List.of(resourceResult));

    when(resourceResult.getResourceId())
        .thenReturn(
            "/subscriptions/sub/resourceGroups/rg/"
                + "providers/Microsoft.Compute/"
                + "virtualMachines/vm-01");

    when(resourceResult.getMetrics()).thenReturn(List.of(metricResult));

    when(metricResult.getMetricName()).thenReturn("Percentage CPU");

    when(metricResult.getTimeSeries()).thenReturn(List.of(series));

    when(series.getValues()).thenReturn(null);

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreMetricValueWithNullTimestamp() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    MetricsClient client = mock(MetricsClient.class);

    MetricsQueryResourcesResult result = mock(MetricsQueryResourcesResult.class);

    MetricsQueryResult resourceResult = mock(MetricsQueryResult.class);

    MetricResult metricResult = mock(MetricResult.class);

    TimeSeriesElement series = mock(TimeSeriesElement.class);

    MetricValue value = mock(MetricValue.class);

    Response<MetricsQueryResourcesResult> response = mock(Response.class);

    when(response.getValue()).thenReturn(result);

    when(result.getMetricsQueryResults()).thenReturn(List.of(resourceResult));

    when(resourceResult.getResourceId())
        .thenReturn(
            "/subscriptions/sub/resourceGroups/rg/"
                + "providers/Microsoft.Compute/"
                + "virtualMachines/vm-01");

    when(resourceResult.getMetrics()).thenReturn(List.of(metricResult));

    when(metricResult.getMetricName()).thenReturn("Percentage CPU");

    when(metricResult.getTimeSeries()).thenReturn(List.of(series));

    when(series.getValues()).thenReturn(List.of(value));

    when(value.getTimeStamp()).thenReturn(null);

    when(client.queryResourcesWithResponse(
            anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any()))
        .thenReturn(response);

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertTrue(usages.isEmpty());
    }
  }

  // Metric value extraction from ranging azure return types

  @Test
  void collectMetrics_shouldUseAverageValue() {
    List<UsageRecordModel> usages =
        collectSingleMetricWithValueSetup(
            value -> {
              when(value.getAverage()).thenReturn(10.0);
            });

    assertEquals(1, usages.size());
    assertEquals(10.0, usages.get(0).getValue());
  }

  @Test
  void collectMetrics_shouldFallbackToTotalWhenAverageMissing() {
    List<UsageRecordModel> usages =
        collectSingleMetricWithValueSetup(
            value -> {
              when(value.getAverage()).thenReturn(null);

              when(value.getTotal()).thenReturn(20.0);
            });

    assertEquals(1, usages.size());
    assertEquals(20.0, usages.get(0).getValue());
  }

  @Test
  void collectMetrics_shouldFallbackToMaximumWhenAverageAndTotalMissing() {
    List<UsageRecordModel> usages =
        collectSingleMetricWithValueSetup(
            value -> {
              when(value.getAverage()).thenReturn(null);

              when(value.getTotal()).thenReturn(null);

              when(value.getMaximum()).thenReturn(30.0);
            });

    assertEquals(1, usages.size());
    assertEquals(30.0, usages.get(0).getValue());
  }

  @Test
  void collectMetrics_shouldFallbackToMinimumWhenPreviousValuesMissing() {
    List<UsageRecordModel> usages =
        collectSingleMetricWithValueSetup(
            value -> {
              when(value.getAverage()).thenReturn(null);

              when(value.getTotal()).thenReturn(null);

              when(value.getMaximum()).thenReturn(null);

              when(value.getMinimum()).thenReturn(40.0);
            });

    assertEquals(1, usages.size());
    assertEquals(40.0, usages.get(0).getValue());
  }

  @Test
  void collectMetrics_shouldFallbackToCountWhenOtherValuesMissing() {
    List<UsageRecordModel> usages =
        collectSingleMetricWithValueSetup(
            value -> {
              when(value.getAverage()).thenReturn(null);

              when(value.getTotal()).thenReturn(null);

              when(value.getMaximum()).thenReturn(null);

              when(value.getMinimum()).thenReturn(null);

              when(value.getCount()).thenReturn(50.0);
            });

    assertEquals(1, usages.size());
    assertEquals(50.0, usages.get(0).getValue());
  }

  @Test
  void collectMetrics_shouldIgnoreMetricValueWhenAllValuesMissing() {
    List<UsageRecordModel> usages =
        collectSingleMetricWithValueSetup(
            value -> {
              when(value.getAverage()).thenReturn(null);

              when(value.getTotal()).thenReturn(null);

              when(value.getMaximum()).thenReturn(null);

              when(value.getMinimum()).thenReturn(null);

              when(value.getCount()).thenReturn(null);
            });

    assertTrue(usages.isEmpty());
  }

  // Resource type handling

  @Test
  void collectMetrics_shouldFallbackToServiceNameWhenResourceTypeMissing() {
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

    when(value.getAverage()).thenReturn(73.5);

    when(series.getValues()).thenReturn(List.of(value));

    when(series.getMetadata()).thenReturn(Map.of());

    when(metricResult.getMetricName()).thenReturn("Percentage CPU");

    when(metricResult.getResourceType()).thenReturn(null);

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

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertEquals(1, usages.size());

      assertEquals("Microsoft.Compute/virtualMachines", usages.get(0).getResourceType());
    }
  }

  @Test
  void collectMetrics_shouldFallbackToServiceNameWhenResourceTypeBlank() {
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

    when(value.getAverage()).thenReturn(73.5);

    when(series.getValues()).thenReturn(List.of(value));

    when(series.getMetadata()).thenReturn(Map.of());

    when(metricResult.getMetricName()).thenReturn("Percentage CPU");

    when(metricResult.getResourceType()).thenReturn(" ");

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

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertEquals(1, usages.size());

      assertEquals("Microsoft.Compute/virtualMachines", usages.get(0).getResourceType());
    }
  }

  @Test
  void collectMetrics_shouldReturnEmptyDimensionsWhenMetadataMissing() {
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

    when(value.getAverage()).thenReturn(73.5);

    when(series.getValues()).thenReturn(List.of(value));

    when(series.getMetadata()).thenReturn(null);

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

      List<UsageRecordModel> usages = provider.collectMetrics(scope, request);

      assertEquals(1, usages.size());
      assertNotNull(usages.get(0).getDimensions());
      assertTrue(usages.get(0).getDimensions().isEmpty());
    }
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
