package com.cloudsherpa.service.analytics.service;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.dto.ResourceNameDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ResourceRegistryService {

  private ConcurrentMap<UUID, ConcurrentMap<UUID, Resource>> resourceRegistry;

  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;
  private final ResourceRepository resourceRepository;

  private final Logger logger = LoggerFactory.getLogger(ResourceRegistryService.class);

  public ResourceRegistryService(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      ResourceRepository resourceRepository) {
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.resourceRepository = resourceRepository;

    resourceRegistry = new ConcurrentHashMap<>();
  }

  public void populateRegistryForUser(UUID userId) {
    ConcurrentMap<UUID, Resource> userRegistry =
        resourceRegistry.computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>());

    List<CloudConnection> userCloudConnections = cloudConnectionRepository.findByUserId(userId);
    int newResources = 0;

    for (CloudConnection userCloudConnection : userCloudConnections) {
      List<CloudAccount> connectionCloudAccounts =
          cloudAccountRepository.findByConnectionId(userCloudConnection.getId());
      for (CloudAccount cloudAccount : connectionCloudAccounts) {
        List<Resource> accountResources = resourceRepository.findByAccountId(cloudAccount.getId());
        for (Resource resource : accountResources) {
          userRegistry.computeIfAbsent(resource.getId(), ignored -> resource);
          newResources++;
        }
      }
    }

    logger.info("Added '{}'' new resources to registry for user '{}'", newResources, userId);
  }

  public List<ResourceNameDto> getResourceNamesByUserId(UUID userId) {
    if (!resourceRegistry.containsKey(userId)) {
      populateRegistryForUser(userId);
      if (!resourceRegistry.containsKey(userId)) {
        return List.of();
      }
    }

    ConcurrentMap<UUID, Resource> userResourceRegistry = resourceRegistry.get(userId);

    List<ResourceNameDto> resourceNameDtos = new ArrayList<>();

    for (Resource userResource : userResourceRegistry.values()) {
      resourceNameDtos.add(
          new ResourceNameDto(userResource.getId().toString(), userResource.getResourceName()));
    }

    return resourceNameDtos;
  }

  public void removeResource(UUID userId, UUID resourceId) {
    if (resourceRegistry.containsKey(userId)) {
      resourceRegistry.get(userId).remove(resourceId);
      logger.info("Removed resource '{}' from registry for user '{}'", resourceId, userId);
    }
  }

  public void addResource(UUID userId, Resource resource) {
    if (!resourceRegistry.containsKey(userId)) {
      populateRegistryForUser(userId);
    }

    resourceRegistry
        .computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>())
        .put(resource.getId(), resource);
  }

  public void updateRegistryAfterAccountDelete(UUID userId) {
    if (!resourceRegistry.containsKey(userId)) {
      return;
    }

    clearAndAuthoritativeRead(userId);
  }

  private void clearAndAuthoritativeRead(UUID userId) {
    if (resourceRegistry.containsKey(userId)) {
      resourceRegistry.get(userId).clear();
    }
    populateRegistryForUser(userId);
  }
}
