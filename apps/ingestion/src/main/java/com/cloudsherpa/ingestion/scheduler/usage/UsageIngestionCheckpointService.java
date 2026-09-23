package com.cloudsherpa.ingestion.scheduler.usage;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UsageIngestionCheckpointService {

  private final CloudAccountRepository cloudAccountRepository;

  public UsageIngestionCheckpointService(CloudAccountRepository cloudAccountRepository) {
    this.cloudAccountRepository = cloudAccountRepository;
  }

  @Transactional
  public void updateCheckpoint(
      UUID accountId, Instant ingestionEndTime, Instant nextIngestionStartTime) {
    CloudAccount account =
        cloudAccountRepository
            .findById(accountId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cloud account not found: " + accountId));

    account.setLastUsageIngestion(ingestionEndTime.atOffset(ZoneOffset.UTC));

    account.setNextUsageIngestion(nextIngestionStartTime.atOffset(ZoneOffset.UTC));

    // The first successful ingestion run for an account is the backfill.
    if (!account.isBackfillCompleted()) {
      account.setBackfillCompleted(true);
    }

    cloudAccountRepository.save(account);
  }
}
