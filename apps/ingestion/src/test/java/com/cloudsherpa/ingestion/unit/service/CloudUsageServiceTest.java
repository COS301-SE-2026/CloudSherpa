package com.cloudsherpa.ingestion.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.*;
import com.cloudsherpa.ingestion.normalization.persistence.service.SherpaDbPersistenceService;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CloudUsageServiceTest {

  private CloudConnectorFactory factory;
  private SherpaDbPersistenceService persistenceService;
  private CloudUsageService service;

  @BeforeEach
  void setUp() {

    factory = mock(CloudConnectorFactory.class);
    persistenceService = mock(SherpaDbPersistenceService.class);

    service = new CloudUsageService(factory, persistenceService);
  }

  @Test
  void ingestShouldFetchUsageRecords() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    UsageRecordModel usageRecord = buildUsageRecord();
    String resource = "resource1";
    ResourceDetail resourceDetail = new ResourceDetail("resourceId", "name", "type", null);

    doReturn(List.of(usageRecord)).when(connector).fetchUsage(any(), any());
    doReturn(List.of(resource)).when(connector).getAllOfferedServices();
    doReturn(List.of(resourceDetail)).when(connector).getAllResources(any());

    IngestionRequestEvent request = buildRequest(true, false);

    IngestionResult result = service.ingest(request);

    assertEquals(1, result.getUsage().size());
    assertEquals(0, result.getBilling().size());

    verify(connector, times(1)).fetchUsage(any(), any());
  }

  @Test
  void ingestShouldFetchBillingRecords() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    BillingRecordModel billing = new BillingRecordModel();

    doReturn(List.of(billing)).when(connector).fetchBilling(any(), any());

    IngestionRequestEvent request = buildRequest(false, true);

    IngestionResult result = service.ingest(request);

    assertEquals(0, result.getUsage().size());
    assertEquals(1, result.getBilling().size());

    verify(connector, times(1)).fetchBilling(any(), any());
  }

  @Test
  void ingestShouldFetchUsageAndBilling() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    doReturn(List.of(buildUsageRecord())).when(connector).fetchUsage(any(), any());

    doReturn(List.of(new BillingRecordModel())).when(connector).fetchBilling(any(), any());

    IngestionRequestEvent request = buildRequest(true, true);

    IngestionResult result = service.ingest(request);

    assertEquals(1, result.getUsage().size());
    assertEquals(1, result.getBilling().size());
  }

  @Test
  void ingestShouldHandleEmptyUsageRecords() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    doReturn(List.of()).when(connector).fetchUsage(any(), any());

    IngestionResult result = service.ingest(buildRequest(true, false));

    assertTrue(result.getUsage().isEmpty());

    verify(connector, times(1)).fetchUsage(any(), any());
  }

  @Test
  void ingestShouldNotCallUsageWhenDisabled() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    service.ingest(buildRequest(false, false));

    verify(connector, never()).fetchUsage(any(), any());
  }

  @Test
  void ingestMockShouldGenerateMockUsage() {

    IngestionResult result = service.ingestMock(buildRequest(true, false));

    assertNotNull(result);
    assertNotNull(result.getUsage());
  }

  @Test
  void ingestMockShouldNotGenerateUsageWhenDisabled() {

    IngestionResult result = service.ingestMock(buildRequest(false, false));

    assertTrue(result.getUsage().isEmpty());
  }

  @Test
  void ingestShouldPersistNormalizedMetrics() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    doReturn(List.of(buildUsageRecord())).when(connector).fetchUsage(any(), any());

    assertDoesNotThrow(() -> service.ingest(buildRequest(true, false)));

    verify(connector, times(1)).fetchUsage(any(), any());
  }

  @Test
  void ingestShouldContinueWhenPersistenceFails() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    doReturn(List.of(buildUsageRecord())).when(connector).fetchUsage(any(), any());

    doThrow(new RuntimeException("DB Failure"))
        .when(persistenceService)
        .recordMetric(any(), any(), any());

    assertDoesNotThrow(() -> service.ingest(buildRequest(true, false)));
  }

  @Test
  void ingestShouldHandleMultipleScopes() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector(anyString())).thenReturn(connector);

    doReturn(List.of(buildUsageRecord())).when(connector).fetchUsage(any(), any());

    IngestionRequestEvent request = buildRequest(true, false);

    AccountScope secondScope = new AccountScope();
    secondScope.setProvider("AWS");
    secondScope.setAccountId("456");

    List<AccountScope> scopes = new ArrayList<>(request.getScopes());
    scopes.add(secondScope);

    request.setScopes(scopes);

    IngestionResult result = service.ingest(request);

    assertEquals(2, result.getUsage().size());
  }

  private UsageRecordModel buildUsageRecord() {

    UsageRecordModel usageRecord = new UsageRecordModel();

    usageRecord.setProvider("AWS");
    usageRecord.setMetricName("CPUUtilization");
    usageRecord.setServiceName("EC2");
    usageRecord.setAccountId("123");
    usageRecord.setTimestamp(Instant.now());
    usageRecord.setValue(50.0);

    return usageRecord;
  }

  private IngestionRequestEvent buildRequest(boolean usage, boolean billing) {

    IngestionRequestEvent request = new IngestionRequestEvent();

    request.setIncludeUsage(usage);
    request.setIncludeBilling(billing);
    request.setUserId(UUID.randomUUID());

    AccountScope scope = new AccountScope();
    scope.setProvider("AWS");
    scope.setAccountId("123");

    request.setScopes(List.of(scope));

    return request;
  }

  static class TestConnector implements CloudConnector, UsageCapable, BillingCapable {

    @Override
    public boolean testConnection(CloudCredentials credentials) {
      return true;
    }

    @Override
    public String getProviderName() {
      return "AWS";
    }

    @Override
    public List<String> getAllOfferedServices() {
      return List.of();
    }

    @Override
    public List<ResourceDetail> getAllResources(CloudCredentials credentials) {
      return List.of();
    }

    @Override
    public List<UsageRecordModel> fetchUsage(
        AccountScope accountScope, IngestionRequestEvent request) {
      return List.of();
    }

    @Override
    public List<UsageRecordModel> fetchMockUsage(
        AccountScope accountScope, IngestionRequestEvent request) {
      return List.of(buildMockRecord());
    }

    @Override
    public List<BillingRecordModel> fetchBilling(
        AccountScope accountScope, IngestionRequestEvent request) {
      return List.of();
    }

    @Override
    public List<BillingRecordModel> fetchMockBilling(
        AccountScope accountScope, IngestionRequestEvent request) {
      return List.of(new BillingRecordModel());
    }

    private static UsageRecordModel buildMockRecord() {

      UsageRecordModel usageRecord = new UsageRecordModel();

      usageRecord.setProvider("AWS");
      usageRecord.setMetricName("CPUUtilization");
      usageRecord.setServiceName("EC2");
      usageRecord.setAccountId("123");
      usageRecord.setTimestamp(Instant.now());
      usageRecord.setValue(42.0);

      return usageRecord;
    }
  }
}
