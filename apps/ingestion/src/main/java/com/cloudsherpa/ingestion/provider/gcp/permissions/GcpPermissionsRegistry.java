package com.cloudsherpa.ingestion.provider.gcp.permissions;

import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceDiscoveryService;
import com.cloudsherpa.ingestion.provider.permissions.PermissionsRegistry;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GcpPermissionsRegistry extends PermissionsRegistry {

  private final Map<String, Set<String>> registry;

  public GcpPermissionsRegistry(GcpResourceDiscoveryService discoveryRegistryProvider) {
    super(COMMON_READ_ONLY);
    registry =
        mergePermissionMaps(
            discoveryRegistryProvider.getPermissionsRegistry(),
            Map.of("bigquery", Set.of("BigQuery Data Viewer", "BigQuery Job User")));
  }

  public static final Set<String> COMMON_READ_ONLY =
      Set.of(
          // Cloud Monitoring
          "Monitoring Viewer",

          // Cloud Asset Inventory
          "Cloud Asset Viewer",

          // Service Usage
          "Service Usage Viewer");

  public Map<String, Set<String>> getRegistry() {
    return registry;
  }
}
