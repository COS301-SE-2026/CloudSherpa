package com.cloudsherpa.ingestion.integration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudConnectorFactory;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.IngestionResult;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.normalizers.AwsNormalizer;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import com.cloudsherpa.ingestion.normalization.normalizers.NormalizerFactory;
import com.cloudsherpa.ingestion.provider.aws.AwsCloudConnector;
import com.cloudsherpa.ingestion.provider.aws.monitoring.MockCloudWatchMetricProvider;
import com.cloudsherpa.ingestion.provider.scanner.ResourceDiscoveryService;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CloudUsageServiceIntegrationTest {

  private CloudConnectorFactory factory;
  private SherpaDbPersistenceService persistenceService;
  private AwsCloudConnector connector;
  private CloudUsageService service;
  private ResourceDiscoveryService discoveryService;
  private NormalizerFactory normalizerFactory;
  private Normalizer normalizer;
  private MockCloudWatchMetricProvider mockMetricProvider;

  @BeforeEach
  void setUp() {

    factory = mock(CloudConnectorFactory.class);
    persistenceService = mock(SherpaDbPersistenceService.class);
    normalizerFactory = mock(NormalizerFactory.class);
    normalizer = mock(AwsNormalizer.class);

    connector = spy(new AwsCloudConnector(discoveryService, mockMetricProvider));

    when(factory.getConnector("AWS")).thenReturn(connector);
    when(normalizerFactory.getNormalizer("AWS")).thenReturn(normalizer);
    when(normalizer.normalize(any(UsageRecordModel.class)))
        .thenReturn(mock(NormalizedMetric.class));

    service = new CloudUsageService(factory, persistenceService, normalizerFactory);
  }

  @Test
  void ingestShouldIntegrateWithAwsConnector() {

    doReturn(List.of(new UsageRecordModel())).when(connector).fetchMockUsage(any(), any());

    IngestionRequestEvent request = new IngestionRequestEvent();

    request.setIncludeUsage(true);

    AccountScope scope = new AccountScope();
    scope.setProvider("AWS");

    request.setScopes(List.of(scope));

    IngestionResult result = service.ingestMockWithNoise(request);

    assertEquals(1, result.getUsage().size());

    verify(connector).fetchMockUsage(any(), any());
    verify(normalizerFactory).getNormalizer("AWS");
    verify(normalizer).normalize(any(UsageRecordModel.class));
    verify(persistenceService).recordMetric(any(), any(), any());
  }
}
