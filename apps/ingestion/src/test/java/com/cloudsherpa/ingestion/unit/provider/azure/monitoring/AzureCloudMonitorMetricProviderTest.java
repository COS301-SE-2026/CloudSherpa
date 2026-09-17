package com.cloudsherpa.ingestion.unit.provider.azure.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.azure.core.http.rest.Response;
import com.azure.monitor.query.metrics.MetricsClient;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesResult;
import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.Instance;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import com.cloudsherpa.ingestion.provider.azure.factory.AzureClientFactory;
import com.cloudsherpa.ingestion.provider.azure.monitoring.AzureCloudMonitorMetricProvider;
import com.cloudsherpa.ingestion.service.IngestionPersistenceService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AzureCloudMonitorMetricProviderTest {

  private AzureCloudMonitorMetricProvider provider;
  private IngestionPersistenceService persistenceService = mock(IngestionPersistenceService.class);

  private Normalizer normalizer = mock(Normalizer.class);

  @BeforeEach
  void setUp() {
    provider = new AzureCloudMonitorMetricProvider(persistenceService);
  }

  // Validation tests

  @Test
  void collectMetrics_shouldThrow_whenAccountScopeIsNull() {
    IngestionRequestEvent request = validRequest();

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(null, request, normalizer));

    assertEquals("Account scope cannot be null", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenRequestIsNull() {
    AccountScope scope = validScope();

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> provider.collectMetrics(scope, null, normalizer));

    assertEquals("Ingestion request cannot be null", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenCredentialsMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();
    request.setCredentials(null);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Azure credentials are required for Azure metric ingestion", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenSubscriptionMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.getCredentials().setSubscriptionId(" ");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Azure subscriptionId is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenTenantMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.getCredentials().setTenantId(" ");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Azure tenantId is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenClientIdMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.getCredentials().setClientId(null);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Azure clientId is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenClientSecretMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.getCredentials().setClientSecret(" ");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Azure clientSecret is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenAccountIdMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    scope.setAccountId(" ");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("AccountId is required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenFromMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setFrom(null);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Metric request from/to timestamps are required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenToMissing() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setTo(null);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Metric request from/to timestamps are required", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenFromAfterTo() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setFrom(Instant.parse("2026-01-01T02:00:00Z"));
    request.setTo(Instant.parse("2026-01-01T01:00:00Z"));

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

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
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Metric request 'from' must be before 'to'", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenPeriodIsZero() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setPeriod(0);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Metric period must be > 0", ex.getMessage());
  }

  @Test
  void collectMetrics_shouldThrow_whenPeriodIsNegative() {
    AccountScope scope = validScope();
    IngestionRequestEvent request = validRequest();

    request.setPeriod(-1);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertEquals("Metric period must be > 0", ex.getMessage());
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

      provider.collectMetrics(scope, request, normalizer);

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

      provider.collectMetrics(scope, request, normalizer);

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

      provider.collectMetrics(scope, request, normalizer);

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

      provider.collectMetrics(scope, request, normalizer);

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

      provider.collectMetrics(scope, request, normalizer);

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
    MetricsClient client = mock(MetricsClient.class);

    invalid.setIdentifier(null);
    invalid.setRegion("westeurope");

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setInstances(List.of(invalid));

    scope.getServiceScopes().get(0).setInstances(List.of(instanceScope));

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreBlankInstanceIdentifier() {
    AccountScope scope = validScope();

    Instance invalid = new Instance();
    MetricsClient client = mock(MetricsClient.class);

    invalid.setIdentifier(" ");
    invalid.setRegion("westeurope");

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setInstances(List.of(invalid));

    scope.getServiceScopes().get(0).setInstances(List.of(instanceScope));

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldThrowWhenRegionMissing() {
    AccountScope scope = validScope();

    scope.getServiceScopes().get(0).getInstances().get(0).getInstances().get(0).setRegion(" ");

    IngestionRequestEvent request = validRequest();

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> provider.collectMetrics(scope, request, normalizer));

    assertTrue(ex.getMessage().contains("Azure resource is missing its region"));
  }

  // Null/empty service or instance scope, or empty metric lists

  @Test
  void collectMetrics_shouldSkipNullServiceScope() {
    AccountScope scope = validScope();
    MetricsClient client = mock(MetricsClient.class);

    scope.setServiceScopes(Collections.singletonList(null));

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldSkipServiceWithoutInstances() {
    AccountScope scope = validScope();
    MetricsClient client = mock(MetricsClient.class);

    scope.getServiceScopes().get(0).setInstances(List.of());

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldSkipServiceWithNullInstances() {
    AccountScope scope = validScope();
    MetricsClient client = mock(MetricsClient.class);

    scope.getServiceScopes().get(0).setInstances(null);

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldSkipServiceWithoutMetrics() {
    AccountScope scope = validScope();
    MetricsClient client = mock(MetricsClient.class);

    scope.getServiceScopes().get(0).setMetrics(List.of());

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldSkipServiceWithNullMetrics() {
    AccountScope scope = validScope();
    MetricsClient client = mock(MetricsClient.class);

    scope.getServiceScopes().get(0).setMetrics(null);

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldSkipServiceWithBlankName() {
    AccountScope scope = validScope();
    MetricsClient client = mock(MetricsClient.class);

    scope.getServiceScopes().get(0).setName(" ");

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldSkipServiceWithNullName() {
    AccountScope scope = validScope();
    MetricsClient client = mock(MetricsClient.class);

    scope.getServiceScopes().get(0).setName(null);

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreNullInstanceScope() {
    AccountScope scope = validScope();
    MetricsClient client = mock(MetricsClient.class);

    scope.getServiceScopes().get(0).setInstances(Collections.singletonList(null));

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  @Test
  void collectMetrics_shouldIgnoreInstanceScopeWithNullInstances() {
    AccountScope scope = validScope();
    MetricsClient client = mock(MetricsClient.class);

    InstanceScope instanceScope = new InstanceScope();

    instanceScope.setInstances(null);

    scope.getServiceScopes().get(0).setInstances(List.of(instanceScope));

    IngestionRequestEvent request = validRequest();

    try (MockedStatic<AzureClientFactory> factory = mockStatic(AzureClientFactory.class)) {

      factory
          .when(() -> AzureClientFactory.createMetricsClient(any(), eq("westeurope")))
          .thenReturn(client);

      provider.collectMetrics(scope, request, normalizer);

      verify(client, times(0))
          .queryResourcesWithResponse(
              anyList(), anyList(), anyString(), any(MetricsQueryResourcesOptions.class), any());
    }
  }

  // Helper functions

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
