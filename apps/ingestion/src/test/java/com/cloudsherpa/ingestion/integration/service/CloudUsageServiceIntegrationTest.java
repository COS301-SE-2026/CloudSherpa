package com.cloudsherpa.ingestion.integration.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudConnectorFactory;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.IngestionResult;
import com.cloudsherpa.ingestion.normalization.normalizers.AwsNormalizer;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import com.cloudsherpa.ingestion.normalization.normalizers.NormalizerFactory;
import com.cloudsherpa.ingestion.provider.aws.AwsCloudConnector;
import com.cloudsherpa.ingestion.provider.aws.monitoring.MockCloudWatchMetricProvider;
import com.cloudsherpa.ingestion.service.CloudUsageService;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CloudUsageServiceIntegrationTest {

  private CloudConnectorFactory factory;
  private AwsCloudConnector connector;
  private CloudUsageService service;
  private NormalizerFactory normalizerFactory;
  private Normalizer normalizer;
  private MockCloudWatchMetricProvider mockMetricProvider;

  @BeforeEach
  void setUp() {
    factory = mock(CloudConnectorFactory.class);
    normalizerFactory = mock(NormalizerFactory.class);
    normalizer = mock(AwsNormalizer.class);
    mockMetricProvider = mock(MockCloudWatchMetricProvider.class);

    connector = new AwsCloudConnector(null, mockMetricProvider, null);

    when(factory.getConnector("AWS")).thenReturn(connector);
    when(normalizerFactory.getNormalizer("AWS")).thenReturn(normalizer);

    service =
        new CloudUsageService(factory, mock(SherpaDbPersistenceService.class), normalizerFactory);
  }

  @Test
  void ingestShouldIntegrateWithAwsConnector() {
    IngestionRequestEvent request = new IngestionRequestEvent();
    request.setIncludeUsage(true);

    AccountScope scope = new AccountScope();
    scope.setProvider("AWS");

    request.setScopes(List.of(scope));

    IngestionResult result = service.ingestMockWithNoise(request);

    assertNotNull(result);
    assertTrue(result.getUsage().isEmpty());
    assertTrue(result.getBilling().isEmpty());

    verify(factory).getConnector("AWS");
    verify(normalizerFactory).getNormalizer("AWS");
    verify(mockMetricProvider).collectMetrics(scope, request, normalizer);
  }
}
