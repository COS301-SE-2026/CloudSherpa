package com.cloudsherpa.ingestion.unit.scheduling.usage;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.scheduler.usage.UsageIngestionJob;
import com.cloudsherpa.ingestion.scheduler.usage.UsageScheduler;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import java.util.List;
import java.util.UUID;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsageSchedulerTest {

  @Mock private CloudAccountRepository repository;

  @Mock private JobScheduler jobScheduler;

  @Mock private UsageIngestionJob usageIngestionJob;

  @Mock private CloudAccount account1;

  @Mock private CloudAccount account2;

  @InjectMocks private UsageScheduler scheduler;

  @Test
  void scheduleUsageJobs_shouldFindDueActiveAccounts() {
    when(repository.findAccountsDueForUsageIngestion(any(), eq(StatusEnum.active)))
        .thenReturn(List.of());

    scheduler.scheduleUsageJobs();

    verify(repository).findAccountsDueForUsageIngestion(any(), eq(StatusEnum.active));
  }

  @Test
  void scheduleUsageJobs_shouldEnqueueJobForEachDueAccount() throws Exception {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    when(account1.getId()).thenReturn(id1);
    when(account2.getId()).thenReturn(id2);

    when(repository.findAccountsDueForUsageIngestion(any(), eq(StatusEnum.active)))
        .thenReturn(List.of(account1, account2));

    scheduler.scheduleUsageJobs();

    ArgumentCaptor<JobLambda> captor = ArgumentCaptor.forClass(JobLambda.class);

    verify(jobScheduler, times(2)).enqueue(captor.capture());

    List<JobLambda> jobs = captor.getAllValues();

    jobs.get(0).run();
    jobs.get(1).run();

    verify(usageIngestionJob).ingest(id1);
    verify(usageIngestionJob).ingest(id2);
  }

  @Test
  void scheduleUsageJobs_shouldDoNothingWhenNoAccountsAreDue() {
    when(repository.findAccountsDueForUsageIngestion(any(), eq(StatusEnum.active)))
        .thenReturn(List.of());

    scheduler.scheduleUsageJobs();
    ArgumentCaptor<JobLambda> jobCaptor = ArgumentCaptor.forClass(JobLambda.class);

    verify(jobScheduler, never()).enqueue(jobCaptor.capture());
  }
}
