package com.cloudsherpa.service.persistconnection.service;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.service.ResourceRegistryService;
import com.cloudsherpa.service.persistconnection.dto.ResourceSelectionDto;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public abstract class ConnectionPersistenceService {
  private final CloudAccountRepository cloudAccountRepository;
  private final ResourceRepository resourceRepository;
  private final ResourceRegistryService resourceRegistryService;

  public ConnectionPersistenceService(
      CloudAccountRepository cloudAccountRepository,
      ResourceRepository resourceRepository,
      ResourceRegistryService resourceRegistryService) {
    this.cloudAccountRepository = cloudAccountRepository;
    this.resourceRepository = resourceRepository;
    this.resourceRegistryService = resourceRegistryService;
  }

  @Transactional
  public boolean updateAccountName(UUID userId, UUID accountId, String name) {
    Optional<CloudAccount> accountOpt = cloudAccountRepository.findById(accountId);

    if (accountOpt.isEmpty()) {
      return false;
    }
    CloudAccount account = accountOpt.get();

    UUID accountOwnerId = account.getConnection().getUserId();

    if (!accountOwnerId.equals(userId)) {
      return false;
    }
    account.setDisplayName(name);
    cloudAccountRepository.save(account);

    return true;
  }

  @Transactional
  public boolean deleteAccount(UUID userId, UUID accountId) {
    Optional<CloudAccount> accountOpt = cloudAccountRepository.findById(accountId);

    if (accountOpt.isEmpty()) {
      return false;
    }
    CloudAccount account = accountOpt.get();

    UUID accountOwnerId = account.getConnection().getUserId();

    if (!accountOwnerId.equals(userId)) {
      return false;
    }
    resourceRepository.findByAccountId(accountId).forEach(resourceRepository::delete);
    cloudAccountRepository.delete(account);
    resourceRegistryService.updateRegistryAfterAccountDelete(userId);

    return true;
  }

  @Transactional
  public boolean updateResourceStatus(UUID userId, UUID resourceId, StatusEnum status) {

    Optional<Resource> resourceOpt = resourceRepository.findById(resourceId);

    if (resourceOpt.isEmpty()) {
      return false;
    }

    Resource resource = resourceOpt.get();

    UUID resourceOwnerId = resource.getAccount().getConnection().getUserId();

    if (!resourceOwnerId.equals(userId)) {
      return false;
    }

    resource.setStatus(status);

    resourceRepository.save(resource);

    return true;
  }

  protected void createResources(
      UUID userId, CloudAccount account, List<ResourceSelectionDto> resources) {
    List<Resource> entities =
        resources.stream()
            .map(
                r ->
                    new Resource.Builder()
                        .id(UUID.randomUUID())
                        .accountId(account.getId())
                        .resourceType(r.serviceType())
                        .resourceName(r.resourceName())
                        .resourceIdentifier(r.resourceId())
                        .resourceIdentifierType(r.resourceType())
                        .region(r.region())
                        .status(r.active() ? StatusEnum.active : StatusEnum.disabled)
                        .tags(r.tags())
                        .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .lastUpdated(OffsetDateTime.now(ZoneOffset.UTC))
                        .build())
            .toList();

    resourceRepository.saveAll(entities);

    for (Resource resource : entities) {
      resourceRegistryService.addResource(userId, resource);
    }
  }
}
