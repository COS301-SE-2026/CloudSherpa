package com.cloudsherpa.ingestion.provider.aws.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.mock.engine.MockMetricEngine;
import com.cloudsherpa.ingestion.provider.monitoring.CloudMonitoringMetricProvider;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockCloudWatchMetricProvider implements CloudMonitoringMetricProvider {

  private final MockMetricEngine metricEngine;

  public MockCloudWatchMetricProvider(AwsMockRegistry registry) {

    this.metricEngine = new MockMetricEngine(registry);
  }

  @Override
  public List<UsageRecordModel> collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request) {

    return metricEngine.collectMetrics(request);
  }
}
