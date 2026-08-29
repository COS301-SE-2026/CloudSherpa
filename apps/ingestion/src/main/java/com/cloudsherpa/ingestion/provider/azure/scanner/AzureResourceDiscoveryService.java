package com.cloudsherpa.ingestion.provider.azure.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.azure.graph.AzureResourceGraphService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AzureResourceDiscoveryService {

  private final AzureResourceGraphService resourceGraphService;
  private final Map<String, AzureResourceScanner> scanners;

  public AzureResourceDiscoveryService(
      AzureResourceGraphService resourceGraphService, List<AzureResourceScanner> scanners) {

    this.resourceGraphService = resourceGraphService;

    this.scanners =
        scanners.stream()
            .flatMap(
                scanner ->
                    scanner.getResourceTypes().stream().map(type -> Map.entry(type, scanner)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public List<ResourceDetail> discover(CloudCredentials credentials, List<String> services) {

    List<JsonNode> resources = resourceGraphService.searchResources(credentials);

    return resources.stream()
        .map(resource -> createResource(resource, credentials, services))
        .filter(Objects::nonNull)
        .toList();
  }

  public List<String> getServices() {
    return scanners.values().stream()
        .map(AzureResourceScanner::getServiceName)
        .distinct()
        .sorted()
        .toList();
  }

  private ResourceDetail createResource(
      JsonNode resource, CloudCredentials credentials, List<String> services) {

    String resourceType = resource.path("type").asText();

    AzureResourceScanner scanner = scanners.get(resourceType);

    if (scanner == null) {
      return null;
    }

    if (!services.contains(scanner.getServiceName())) {
      return null;
    }

    return scanner.scan(resource, credentials);
  }
}
