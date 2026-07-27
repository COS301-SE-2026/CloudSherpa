package com.cloudsherpa.ingestion.scheduler.usage;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.jobrunr.jobs.annotations.Recurring;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;

@Component
public class UsageScheduler {

  private final CloudAccountRepository repository;
  private final JobScheduler jobScheduler;
  private final UsageIngestionJob usageIngestionJob;

  public UsageScheduler(
      JobScheduler jobScheduler,
      CloudAccountRepository repository,
      UsageIngestionJob usageIngestionJob) {
    this.jobScheduler = jobScheduler;
    this.repository = repository;
    this.usageIngestionJob = usageIngestionJob;
  }

  @Recurring(id = "usage-scanner", cron = "*/30 * * * * *") // we run every 30 seconds
  public void scheduleUsageJobs() {

    List<CloudAccount> dueAccounts =
        repository.findAccountsDueForUsageIngestion(
            OffsetDateTime.now(ZoneOffset.UTC), StatusEnum.active);

    dueAccounts.forEach(
        account -> jobScheduler.enqueue(() -> usageIngestionJob.ingest(account.getId())));
  }
}
