package com.cloudsherpa.ingestion.provider.gcp.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.asset.GcpAssetInventoryService;
import com.google.cloud.asset.v1.Asset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    List<Asset> assets = assetInventoryService.listAssets(credentials);

    return assets.stream()
        .map(
            asset -> {
              GcpResourceScanner scanner = findScanner(asset.getAssetType());

              if (scanner == null) { // unsupported resource type
                return null;
              }

              if (!services.contains(
                  scanner.getServiceName())) { // service not selected for discovery
                return null;
              }

              return scanner.scan(asset, credentials);
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
}
