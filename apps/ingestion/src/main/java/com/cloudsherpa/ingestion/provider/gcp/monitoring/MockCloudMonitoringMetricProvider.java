package com.cloudsherpa.ingestion.provider.gcp.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.aws.monitoring.CloudWatchMetricProvider;
import com.cloudsherpa.ingestion.provider.mock.engine.MockMetricEngine;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockCloudMonitoringMetricProvider implements CloudWatchMetricProvider {

  private final MockMetricEngine metricEngine;

  public MockCloudMonitoringMetricProvider(GcpMockRegistry registry) {

    this.metricEngine = new MockMetricEngine(registry);
  }

  @Override
  public List<UsageRecordModel> collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request) {

    return metricEngine.collectMetrics(request);
  }
}
