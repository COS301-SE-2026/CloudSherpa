package com.cloudsherpa.ingestion.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.*;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import com.cloudsherpa.ingestion.normalization.normalizers.NormalizerFactory;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CloudUsageServiceTest {

  private CloudConnectorFactory factory;
  private SherpaDbPersistenceService persistenceService;
  private CloudUsageService service;
  private NormalizerFactory normalizerFactory;
  private Normalizer normalizer;

  @BeforeEach
  void setUp() {

    factory = mock(CloudConnectorFactory.class);
    persistenceService = mock(SherpaDbPersistenceService.class);
    normalizerFactory = mock(NormalizerFactory.class);
    normalizer = mock(Normalizer.class);

    when(normalizerFactory.getNormalizer("AWS")).thenReturn(normalizer);
    when(normalizer.normalize(any(UsageRecordModel.class)))
        .thenReturn(mock(NormalizedMetric.class));

    service = new CloudUsageService(factory, persistenceService, normalizerFactory);
  }

  @Test
  void ingestShouldFetchUsageRecords() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    doNothing().when(connector).fetchUsage(any(), any(), any());

    IngestionResult result = service.ingest(buildRequest(true, false));

    assertNotNull(result);
    assertTrue(result.getUsage().isEmpty());
    assertTrue(result.getBilling().isEmpty());

    verify(connector, times(1)).fetchUsage(any(), any(), any());
    verify(normalizerFactory).getNormalizer("AWS");
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

    doNothing().when(connector).fetchUsage(any(), any(), any());

    doReturn(List.of(new BillingRecordModel())).when(connector).fetchBilling(any(), any());

    IngestionResult result = service.ingest(buildRequest(true, true));

    assertNotNull(result);
    assertTrue(result.getUsage().isEmpty());
    assertEquals(1, result.getBilling().size());

    verify(connector).fetchUsage(any(), any(), any());
    verify(connector).fetchBilling(any(), any());
    verify(normalizerFactory).getNormalizer("AWS");
  }

  @Test
  void ingestShouldHandleEmptyUsageRecords() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    IngestionResult result = service.ingest(buildRequest(true, false));

    assertTrue(result.getUsage().isEmpty());

    verify(connector, times(1)).fetchUsage(any(), any(), any());

    verify(normalizerFactory).getNormalizer("AWS");
    verify(normalizer, never()).normalize(any());
  }

  @Test
  void ingestShouldNotCallUsageWhenDisabled() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector("AWS")).thenReturn(connector);

    service.ingest(buildRequest(false, false));

    verify(connector, never()).fetchUsage(any(), any(), any());
    verify(normalizerFactory).getNormalizer("AWS");
    verify(normalizer, never()).normalize(any());
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

    verify(normalizerFactory, never()).getNormalizer(any());
    verify(normalizer, never()).normalize(any());
  }

  @Test
  void ingestShouldHandleMultipleScopes() {

    TestConnector connector = spy(new TestConnector());

    when(factory.getConnector(anyString())).thenReturn(connector);

    IngestionRequestEvent request = buildRequest(true, false);

    AccountScope secondScope = new AccountScope();
    secondScope.setProvider("AWS");
    secondScope.setAccountId("456");

    List<AccountScope> scopes = new ArrayList<>(request.getScopes());
    scopes.add(secondScope);

    request.setScopes(scopes);

    IngestionResult result = service.ingest(request);

    assertNotNull(result);
    assertTrue(result.getUsage().isEmpty());

    verify(connector, times(2)).fetchUsage(any(), any(), any());
    verify(normalizerFactory, times(2)).getNormalizer("AWS");
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
    public List<ResourceDetail> getAllResources(
        CloudCredentials credentials, List<String> serviceTypes) {
      return List.of();
    }

    @Override
    public void fetchUsage(
        AccountScope accountScope, IngestionRequestEvent request, Normalizer normalizer) {
      return;
    }

    @Override
    public void fetchMockUsage(
        AccountScope accountScope, IngestionRequestEvent request, Normalizer normalizer) {
      return;
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
  }
}
