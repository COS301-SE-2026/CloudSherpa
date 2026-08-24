package com.cloudsherpa.ingestion.provider.gcp.permissions;

import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceDiscoveryService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class GcpPermissionsRegistry {
  private static GcpResourceDiscoveryService discoveryRegistryProvider;

  private GcpPermissionsRegistry() {}

  public static final Set<String> COMMON_READ_ONLY =
      Set.of(
          // Cloud Monitoring
          "monitoring.timeSeries.list",
          "monitoring.metricDescriptors.list",
          "monitoring.metricDescriptors.get",
          "monitoring.monitoredResourceDescriptors.list",
          "monitoring.monitoredResourceDescriptors.get",

          // Cloud Asset Inventory
          "cloudasset.assets.searchAllResources",

          // Service Usage
          "serviceusage.services.use");

  private static final Map<String, Set<String>> REGISTRY =
      mergePermissionMaps(
          discoveryRegistryProvider.getPermissionsRegistry(),
          Map.of(
              "gke_cluster",
              Set.of("container.clusters.get"),
              "cloud_function",
              Set.of("cloudfunctions.functions.get"),
              "cloud_run_service",
              Set.of("run.services.get"),
              "gcs_bucket",
              Set.of("storage.buckets.get"),
              "bigquery",
              Set.of(
                  "bigquery.datasets.get",
                  "bigquery.tables.get",
                  "bigquery.tables.getData",
                  "bigquery.jobs.create")));

  public static Map<String, Set<String>> mergePermissionMaps(
      Map<String, Set<String>> first, Map<String, Set<String>> second) {

    Map<String, Set<String>> result = new HashMap<>(first);

    second.forEach(
        (service, permissions) ->
            result.merge(
                service,
                new HashSet<>(permissions),
                (existing, incoming) -> {
                  existing.addAll(incoming);
                  return existing;
                }));

    return result;
  }

  public static Set<String> getPermissions(String service) {
    return REGISTRY.getOrDefault(service.toLowerCase(Locale.ROOT), Collections.emptySet());
  }

  public static Set<String> getPermissions(Set<String> services) {
    Set<String> permissions = new java.util.HashSet<>(COMMON_READ_ONLY);

    for (String service : services) {
      permissions.addAll(getPermissions(service));
    }

    return Set.copyOf(permissions);
  }
}
