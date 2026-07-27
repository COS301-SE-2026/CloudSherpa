package com.cloudsherpa.ingestion.scheduler.billing;

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
public class BillingScheduler {

  private final CloudAccountRepository repository;
  private final JobScheduler jobScheduler;
  private final BillingIngestionJob billingIngestionJob;

  public BillingScheduler(
      JobScheduler jobScheduler,
      CloudAccountRepository repository,
      BillingIngestionJob billingIngestionJob) {
    this.jobScheduler = jobScheduler;
    this.repository = repository;
    this.billingIngestionJob = billingIngestionJob;
  }

  @Recurring(id = "billing-scanner", cron = "*/300 * * * * *") // we run every 5 minutes
  public void scheduleBillingJobs() {

    List<CloudAccount> dueAccounts = repository.findAccountsDueForBillingIngestion(
        OffsetDateTime.now(ZoneOffset.UTC), StatusEnum.active);

    dueAccounts.forEach(
        account -> jobScheduler.enqueue(() -> billingIngestionJob.ingest(account.getId())));
  }
}
