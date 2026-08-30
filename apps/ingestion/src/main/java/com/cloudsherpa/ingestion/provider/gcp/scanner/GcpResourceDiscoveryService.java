package com.cloudsherpa.ingestion.provider.gcp.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.asset.GcpAssetInventoryService;
import com.google.cloud.asset.v1.ResourceSearchResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GcpResourceDiscoveryService {

  private final GcpAssetInventoryService assetInventoryService;

  private final Map<String, GcpResourceScanner> scanners;

  public GcpResourceDiscoveryService(
      GcpAssetInventoryService assetInventoryService, List<GcpResourceScanner> scanners) {

    this.assetInventoryService = assetInventoryService;

    this.scanners =
        scanners.stream()
            .flatMap(
                scanner -> scanner.getAssetTypes().stream().map(type -> Map.entry(type, scanner)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public List<ResourceDetail> discover(CloudCredentials credentials, List<String> services) {
    List<String> assetTypes =
        scanners.values().stream()
            .map(GcpResourceScanner::getAssetTypes)
            .flatMap(List::stream)
            .toList();

    List<ResourceSearchResult> resources =
        assetInventoryService.searchResources(credentials, assetTypes);

    return resources.stream()
        .map(
            resource -> {
              GcpResourceScanner scanner = findScanner(resource.getAssetType());

              if (scanner == null) { // unsupported resource type
                return null;
              }

              if (!services.contains(
                  scanner.getServiceName())) { // service not selected for discovery
                return null;
              }

              return scanner.scan(resource, credentials);
            })
        .filter(Objects::nonNull)
        .toList();
  }

  public List<String> getServices() {

    return scanners.values().stream()
        .map(GcpResourceScanner::getServiceName)
        .distinct()
        .sorted()
        .toList();
  }

  private GcpResourceScanner findScanner(String assetType) {
    return scanners.get(assetType);
  }

  public Map<String, Set<String>> getPermissionsRegistry() {
    return scanners.values().stream()
        .collect(
            Collectors.toMap(
                GcpResourceScanner::getServiceName, GcpResourceScanner::getPermissionsRequired));
  }
}
