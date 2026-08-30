package com.cloudsherpa.ingestion.provider.scanner;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
// This service automatically injects all ResourceScanner components, allowing
// the implementation of discovery logic alongside the service implementation
// without if/case blocks
public class ResourceDiscoveryService {

  private final Map<ScannerKey, ResourceScanner> scanners;

  public ResourceDiscoveryService(List<ResourceScanner> scanners) {
    this.scanners =
        scanners.stream()
            .collect(
                Collectors.toMap(
                    scanner -> new ScannerKey(scanner.getProvider(), scanner.getServiceName()),
                    Function.identity()));
  }

  public List<ResourceDetail> discover(
      String provider, List<String> services, CloudCredentials credentials) {

    return services.stream()
        .map(service -> scanners.get(new ScannerKey(provider, service)))
        .filter(Objects::nonNull)
        .flatMap(scanner -> scanner.scan(credentials).stream())
        .toList();
  }

  public List<String> getServices(String provider) {
    return scanners.keySet().stream()
        .filter(key -> key.provider().equals(provider))
        .map(ScannerKey::serviceName)
        .toList();
  }

  public Map<String, Set<String>> getPermissionsRegistry() {
    return scanners.values().stream()
        .collect(
            Collectors.toMap(
                ResourceScanner::getServiceName, ResourceScanner::getPermissionsRequired));
  }
}
