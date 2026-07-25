package com.cloudsherpa.ingestion.scheduler.billing;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BillingIngestionService {
  private final BillingIngestionClient client;
  private final CloudAccountRepository cloudAccountRepository;
  private final BillingExportConfigRepository billingConfigRepository;

  public BillingIngestionService(
      BillingIngestionClient client,
      CloudAccountRepository cloudAccountRepository,
      CloudConnectionRepository cloudConnectionRepository,
      BillingExportConfigRepository billingExportConfigRepository) {
    this.client = client;
    this.cloudAccountRepository = cloudAccountRepository;
    this.billingConfigRepository = billingExportConfigRepository;
  }

  @Transactional
  public void ingest(UUID accountId) {
    CloudAccount account =
        cloudAccountRepository
            .findById(accountId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cloud account not found: " + accountId));
    String userId = account.getConnection().getUser().getId().toString();

    billingConfigRepository
        .findByAccountId(accountId)
        .forEach(config -> client.execute(userId, config.getId().toString()));

    Instant ingestionEndTime = Instant.now();
    account.setLastBillingIngestion(ingestionEndTime.atOffset(ZoneOffset.UTC));

    account.setNextBillingIngestion(ingestionEndTime.atOffset(ZoneOffset.UTC).plusHours(6));

    cloudAccountRepository.save(account);
  }
}
