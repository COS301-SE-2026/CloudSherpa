package com.cloudsherpa.ingestion.scheduler.usage;

import java.util.UUID;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Service;

@Service
public class UsageIngestionJob {
  private final UsageIngestionService ingestionService;

  public UsageIngestionJob(UsageIngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @Job(name = "Usage ingestion: %0")
  public void ingest(UUID accountId) {
    ingestionService.ingest(accountId);
  }
}
