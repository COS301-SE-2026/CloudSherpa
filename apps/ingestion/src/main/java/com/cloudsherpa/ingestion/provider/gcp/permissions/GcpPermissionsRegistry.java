package com.cloudsherpa.ingestion.provider.gcp.permissions;

import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceDiscoveryService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GcpPermissionsRegistry {

  private final GcpResourceDiscoveryService discoveryRegistryProvider;

  public GcpPermissionsRegistry(GcpResourceDiscoveryService discoveryRegistryProvider) {
    this.discoveryRegistryProvider = discoveryRegistryProvider;
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
    return mergePermissionMaps(
        discoveryRegistryProvider.getPermissionsRegistry(),
        Map.of("bigquery", Set.of("BigQuery Data Viewer", "BigQuery Job User")));
  }

  public static Map<String, Set<String>> mergePermissionMaps(
      Map<String, Set<String>> first, Map<String, Set<String>> second) {

    Map<String, Set<String>> result = new HashMap<>(first);

    second.forEach(
        (service, permissions) ->
            result.merge(
                service.toLowerCase(Locale.ROOT),
                new HashSet<>(permissions),
                (existing, incoming) -> {
                  existing.addAll(incoming);
                  return existing;
                }));

    return result;
  }

  public Set<String> getPermissions(String service) {
    return getRegistry().getOrDefault(service.toLowerCase(Locale.ROOT), Collections.emptySet());
  }

  public Set<String> getPermissions(Set<String> services) {
    Set<String> permissions = new HashSet<>(COMMON_READ_ONLY);

    for (String service : services) {
      permissions.addAll(getPermissions(service));
    }

    return Set.copyOf(permissions);
  }
}
