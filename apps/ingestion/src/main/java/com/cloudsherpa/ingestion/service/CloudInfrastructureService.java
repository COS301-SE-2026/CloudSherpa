package com.cloudsherpa.ingestion.service;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
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

  @Transactional
  public Resource ensureInfrastructure(UsageRecordModel r, UUID userId) {
    CloudConnection connection = ensureCloudConnection(userId, r.getProvider());
    CloudAccount account = ensureCloudAccount(connection.getId(), r.getAccountId());

    return ensureResource(account.getId(), r.getServiceName(), r.getResourceId(), r.getRegion());
  }

  private CloudConnection ensureCloudConnection(UUID userId, String provider) {
    ProviderEnum providerEnum = ProviderEnum.valueOf(provider.trim().toUpperCase());

    return connectionRepo.findByUserIdAndProvider(userId, providerEnum).stream()
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Cloud connection not found for user " + userId + " and provider " + provider));
  }

  private CloudAccount ensureCloudAccount(UUID connectionId, String cloudAccountId) {
    UUID accountUuid = UUID.fromString(cloudAccountId);

    CloudAccount account =
        accountRepo
            .findById(accountUuid)
            .orElseThrow(
                () -> new IllegalStateException("Cloud account not found: " + cloudAccountId));

    if (!account.getConnectionId().equals(connectionId)) {
      throw new IllegalStateException(
          "Cloud account " + cloudAccountId + " does not belong to connection " + connectionId);
    }

    return account;
  }

  private Resource ensureResource(
      UUID accountId, String resourceType, String resourceIdentifier, String region) {
    return resourceRepo
        .findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            accountId, resourceType, resourceIdentifier, region)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Resource not found for account="
                        + accountId
                        + ", resourceType="
                        + resourceType
                        + ", resourceIdentifier="
                        + resourceIdentifier
                        + ", region="
                        + region));
  }
}
