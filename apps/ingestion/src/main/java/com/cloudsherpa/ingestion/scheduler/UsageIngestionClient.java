package com.cloudsherpa.ingestion.scheduler;

import com.cloudsherpa.ingestion.controller.CloudUsageController;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import org.springframework.stereotype.Service;

@Service
public class UsageIngestionClient {
  private final CloudUsageController usageController;

  public UsageIngestionClient(CloudUsageController usageController) {
    this.usageController = usageController;
  }

  public void ingest(IngestionRequestEvent request) {
    usageController.ingest(request);
  }
}
