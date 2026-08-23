package com.cloudsherpa.ingestion.provider.gcp.permissions;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class GcpPermissionsRegistry {

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
      Map.of(
          "gce_instance",
          Set.of("compute.instances.get"),
          "gke_cluster",
          Set.of("container.clusters.get"),
          "cloud_function",
          Set.of("cloudfunctions.functions.get"),
          "cloud_run_service",
          Set.of("run.services.get"),
          "gcs_bucket",
          Set.of("storage.buckets.get"));

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
