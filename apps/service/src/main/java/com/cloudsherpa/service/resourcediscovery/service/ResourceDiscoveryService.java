package com.cloudsherpa.service.resourcediscovery.service;

import com.cloudsherpa.service.resourcediscovery.client.IngestionResourceClient;
import com.cloudsherpa.service.resourcediscovery.dto.ResourceDetailDto;
import com.cloudsherpa.service.resourcediscovery.dto.ResourceDiscoveryDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResourceDiscoveryService {

  private final IngestionResourceClient ingestionClient;

  public ResourceDiscoveryService(IngestionResourceClient ingestionClient) {
    this.ingestionClient = ingestionClient;
  }

  public List<String> getServices(String provider) {
    return ingestionClient.getServices(provider);
  }

  public List<ResourceDetailDto> getResources(String provider, ResourceDiscoveryDto request) {

    return ingestionClient.getResources(provider, request);
  }

  public String generateAwsPermissions(List<String> services) {

    return ingestionClient.generateAwsPermissions(services);
  }

  public List<String> generateGcpPermissions(List<String> services) {

    return ingestionClient.generateGcpPermissions(services);
  }
}
