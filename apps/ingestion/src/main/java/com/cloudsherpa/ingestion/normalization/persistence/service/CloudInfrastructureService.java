package com.cloudsherpa.ingestion.normalization.persistence.service;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.persistence.entity.CloudAccount;
import com.cloudsherpa.ingestion.normalization.persistence.entity.CloudConnection;
import com.cloudsherpa.ingestion.normalization.persistence.entity.Resource;
import com.cloudsherpa.ingestion.normalization.persistence.repository.CloudAccountRepository;
import com.cloudsherpa.ingestion.normalization.persistence.repository.CloudConnectionRepository;
import com.cloudsherpa.ingestion.normalization.persistence.repository.ResourceRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// This service is to ensure that all cloud entities exist before the metrics normalization
// Creates or fetches CloudConnection, CloudAccount and Resource
@Service
public class CloudInfrastructureService {

  @Autowired private CloudConnectionRepository connectionRepo;
  @Autowired private CloudAccountRepository accountRepo;
  @Autowired private ResourceRepository resourceRepo;

  // Ensures all cloud infrastructure entities exist for the given usage record.

  // Creates missing entities in the hierarchy:
  // CloudConnection -> CloudAccount -> Resource
  @Transactional
  public Resource ensureInfrastructure(UsageRecordModel r, UUID userId) {
    // Step 1: Create/fetch CloudConnection (user -> provider)
    CloudConnection connection = ensureCloudConnection(userId, r.getProvider());

    // Step 2: Create/fetch CloudAccount (connection -> AWS account)
    CloudAccount account = ensureCloudAccount(connection.getId(), r.getAccountId());

    // Step 3: Create/fetch Resource (account -> resource instance)

    return ensureResource(account.getId(), r.getResourceId(), r.getResourceType());
  }

  // Ensures CloudConnection exists for user + provider combination.
  // If it exists, returns it; otherwise creates a new one.
  private CloudConnection ensureCloudConnection(UUID userId, String provider) {
    List<CloudConnection> connection = connectionRepo.findByUserIdAndProvider(userId, provider);

    if (!connection.isEmpty()) {
      return connection.get(0);
    }

    CloudConnection newConnection =
        new CloudConnection(UUID.randomUUID(), userId, provider, "active", OffsetDateTime.now());

    return connectionRepo.save(newConnection);
  }

  // Ensures CloudAccount exists for connection + AWS account ID.
  private CloudAccount ensureCloudAccount(UUID connectionId, String cloudAccountId) {

    // The method converts the cloud provider's string ID into a deterministic UUID
    // This ensures that the same id we get from AWS response always maps to the same UUID
    // in our db to avoid duplicates
    UUID accountUuid = UUID.nameUUIDFromBytes(cloudAccountId.getBytes());

    Optional<CloudAccount> account = accountRepo.findById(accountUuid);
    if (account.isPresent()) {
      return account.get();
    }

    CloudAccount newAccount =
        new CloudAccount(
            accountUuid, connectionId, "aws_account", cloudAccountId, OffsetDateTime.now());

    return accountRepo.save(newAccount);
  }

  // Ensures Resource exists for account + resource ID.
  private Resource ensureResource(UUID accountId, String cloudResourceId, String resourceType) {
    // Generate deterministic UUID from the cloud resource ID string
    UUID resourceUuid = UUID.nameUUIDFromBytes(cloudResourceId.getBytes());

    Optional<Resource> existing = resourceRepo.findById(resourceUuid);
    if (existing.isPresent()) {
      return existing.get();
    }

    Resource newResource =
        new Resource(
            resourceUuid,
            accountId,
            resourceType,
            null, // will need to be populated later
            OffsetDateTime.now());
    return resourceRepo.save(newResource);
  }
}
