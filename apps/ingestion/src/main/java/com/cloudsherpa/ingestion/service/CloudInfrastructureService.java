package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.lib.entities.AccountTypeEnum;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// This service is to ensure that all cloud entities exist before the metrics normalization
// Creates or fetches CloudConnection, CloudAccount and Resource
@Service
public class CloudInfrastructureService {

  private final CloudConnectionRepository connectionRepo;
  private final CloudAccountRepository accountRepo;
  private final ResourceRepository resourceRepo;

  public CloudInfrastructureService(
      CloudConnectionRepository connectionRepo,
      CloudAccountRepository accountRepo,
      ResourceRepository resourceRepo) {
    this.connectionRepo = connectionRepo;
    this.accountRepo = accountRepo;
    this.resourceRepo = resourceRepo;
  }

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
    ProviderEnum providerEnum = ProviderEnum.valueOf(provider.trim().toUpperCase());

    List<CloudConnection> connection = connectionRepo.findByUserIdAndProvider(userId, providerEnum);

    if (!connection.isEmpty()) {
      return connection.get(0);
    }

    CloudConnection newConnection =
        new CloudConnection(
            UUID.randomUUID(), userId, providerEnum, StatusEnum.active, OffsetDateTime.now());

    return connectionRepo.save(newConnection);
  }

  // Ensures CloudAccount exists for connection + AWS account ID.
  private CloudAccount ensureCloudAccount(UUID connectionId, String cloudAccountId) {

    // The method converts the cloud provider's string ID into a deterministic UUID
    // This ensures that the same id we get from AWS response always maps to the
    // same UUID
    // in our db to avoid duplicates
    UUID accountUuid = UUID.nameUUIDFromBytes(cloudAccountId.getBytes());

    Optional<CloudAccount> account = accountRepo.findById(accountUuid);
    if (account.isPresent()) {
      return account.get();
    }

    CloudAccount newAccount =
        new CloudAccount.Builder()
            .id(accountUuid)
            .connectionId(connectionId)
            .accountType(AccountTypeEnum.aws_account)
            .displayName(cloudAccountId)
            .ingestionPeriod(null)
            .createdAt(OffsetDateTime.now())
            .build();

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
        new Resource.Builder()
            .id(resourceUuid)
            .accountId(accountId)
            .resourceType(resourceType)
            .resourceIdentifier(cloudResourceId)
            .resourceIdentifierType("InstanceId")
            .status(StatusEnum.active)
            .region("af-south-1")
            .resourceName("resource")
            .lastUpdated(OffsetDateTime.now())
            .createdAt(OffsetDateTime.now())
            .build();

    return resourceRepo.save(newResource);
  }
}
