package com.cloudsherpa.service.persistconnection.service;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.service.persistconnection.dto.CloudAccountDetailsResponse;
import com.cloudsherpa.service.persistconnection.dto.CloudAccountPatchRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CloudAccountPersistenceService {

  private final CloudAccountRepository cloudAccountRepository;

  public CloudAccountPersistenceService(CloudAccountRepository cloudAccountRepository) {
    this.cloudAccountRepository = cloudAccountRepository;
  }

  @Transactional
  public CloudAccountDetailsResponse updateAccount(
      UUID userId, UUID accountId, CloudAccountPatchRequest request) {

    if (request == null || !request.hasUpdates()) {
      throw new IllegalArgumentException("At least one cloud account field must be supplied");
    }

    CloudAccount account =
        cloudAccountRepository
            .findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new NoSuchElementException("Cloud account not found"));

    if (request.displayName() != null) {
      account.setDisplayName(request.displayName());
    }

    if (request.ingestionBudget() != null) {
      if (request.ingestionBudget() < 0) {
        throw new IllegalArgumentException("ingestionBudget must be greater than or equal to 0");
      }
      account.setIngestionBudget(request.ingestionBudget());
    }

    if (request.ingestionPeriod() != null) {
      if (request.ingestionPeriod() <= 0) {
        throw new IllegalArgumentException("ingestionPeriod must be greater than 0");
      }

      account.setIngestionPeriod(request.ingestionPeriod().toString());
    }

    if (request.periodicResourceDiscovery() != null) {
      boolean previousValue = account.isPeriodicResourceDiscovery();

      boolean newValue = request.periodicResourceDiscovery();

      account.setPeriodicResourceDiscovery(newValue);

      if (!newValue) {
        account.setNextResourceScan(null);
      } else if (!previousValue || account.getNextResourceScan() == null) {
        account.setNextResourceScan(OffsetDateTime.now(ZoneOffset.UTC));
      }
    }

    if (request.autoAdjustIngestionPeriod() != null) {
      account.setAutoAdjustIngestionPeriod(request.autoAdjustIngestionPeriod());
    }

    if (request.newResourcesActive() != null) {
      account.setNewResourcesActive(request.newResourcesActive());
    }

    CloudAccount saved = cloudAccountRepository.save(account);

    return toResponse(saved);
  }

  private CloudAccountDetailsResponse toResponse(CloudAccount account) {

    return new CloudAccountDetailsResponse(
        account.getId(),
        account.getDisplayName(),
        account.getAccountType(),
        account.getConnection().getUser().getEmail(),
        account.getIngestionPeriod(),
        account.getCreatedAt(),
        account.isPeriodicResourceDiscovery(),
        account.isAutoAdjustIngestionPeriod(),
        account.isNewResourcesActive(),
        account.getNextResourceScan());
  }
}
