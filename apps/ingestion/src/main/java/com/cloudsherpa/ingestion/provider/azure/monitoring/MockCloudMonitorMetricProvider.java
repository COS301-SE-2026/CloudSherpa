package com.cloudsherpa.ingestion.provider.azure.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import com.cloudsherpa.ingestion.provider.mock.engine.MockMetricEngine;
import com.cloudsherpa.ingestion.provider.monitoring.CloudMonitoringMetricProvider;
import com.cloudsherpa.ingestion.service.IngestionPersistenceService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockCloudMonitorMetricProvider implements CloudMonitoringMetricProvider {

  private final MockMetricEngine metricEngine;
  private final IngestionPersistenceService persistenceService;

  public MockCloudMonitorMetricProvider(
      AzureMockRegistry registry, IngestionPersistenceService persistenceService) {
    this.persistenceService = persistenceService;
    this.metricEngine = new MockMetricEngine(registry);
  }

  @Override
  public void collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request, Normalizer normalizer) {

    List<UsageRecordModel> usageRecords = metricEngine.collectMetrics(request);
    persistenceService.normalizeAndPersistUsage(usageRecords, request.getUserId(), normalizer);
  }
}
