package com.cloudsherpa.ingestion.provider.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import java.util.List;

public interface CloudMonitoringMetricProvider {

  List<UsageRecordModel> collectMetrics(AccountScope accountScope, IngestionRequestEvent request);
}
