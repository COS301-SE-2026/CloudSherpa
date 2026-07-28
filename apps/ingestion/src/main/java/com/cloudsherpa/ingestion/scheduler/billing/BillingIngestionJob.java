package com.cloudsherpa.ingestion.scheduler.billing;

import java.util.UUID;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Service;

@Service
public class BillingIngestionJob {
  private final BillingIngestionService ingestionService;

  public BillingIngestionJob(BillingIngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @Job(name = "Billing ingestion: %0")
  public void ingest(UUID accountId) {
    ingestionService.ingest(accountId);
  }
}
