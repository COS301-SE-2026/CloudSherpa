package com.cloudsherpa.ingestion.provider.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;

public interface CloudMonitoringMetricProvider {

  void collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request, Normalizer normalizer);
}
