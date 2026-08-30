package com.cloudsherpa.service.metrics;

import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ResourceProviderResolver {
  private final ResourceRepository resourceRepository;

  public ResourceProviderResolver(ResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  public ProviderEnum resolveProvider(UUID resourceId) {
    return resourceRepository.findProviderByResourceId(resourceId);
  }
}
