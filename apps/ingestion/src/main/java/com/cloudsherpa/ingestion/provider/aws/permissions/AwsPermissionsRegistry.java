package com.cloudsherpa.ingestion.provider.aws.permissions;

import com.cloudsherpa.ingestion.provider.scanner.ResourceDiscoveryService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AwsPermissionsRegistry {

  public static final Set<String> COMMON_READ_ONLY =
      Set.of(
          "cloudwatch:GetMetricData",
          "cloudwatch:GetMetricStatistics",
          "cloudwatch:ListMetrics",
          "cloudwatch:DescribeAlarms",
          "cloudwatch:DescribeAlarmHistory",
          "cloudwatch:ListTagsForResource",
          "cloudwatch:ListEntitiesForMetric",
          "logs:DescribeLogGroups",
          "logs:DescribeLogStreams",
          "logs:GetLogEvents",
          "logs:FilterLogEvents",
          "organizations:DescribeOrganization",
          "organizations:DescribeAccount",
          "organizations:ListAccounts",
          "organizations:ListTagsForResource",
          "resource-explorer-2:GetIndex",
          "resource-explorer-2:ListIndexes",
          "resource-explorer-2:Search",
          "tag:GetResources",
          "cur:DescribeReportDefinitions",
          "cur:GetClassicReport",
          "cur:GetUsageReport",
          "cur:ListTagsForResource");

  private final Map<String, Set<String>> registry;

  public AwsPermissionsRegistry(ResourceDiscoveryService discoveryRegistryProvider) {

    this.registry =
        mergePermissionMaps(
            Map.of(
                "S3",
                Set.of(
                    "s3:ListAllMyBuckets",
                    "s3:ListBucket",
                    "s3:GetBucketLocation",
                    "s3:GetBucketTagging",
                    "s3:GetObject")),
            discoveryRegistryProvider.getPermissionsRegistry());
  }

  public Map<String, Set<String>> getRegistry() {
    return registry;
  }

  public static Map<String, Set<String>> mergePermissionMaps(
      Map<String, Set<String>> first, Map<String, Set<String>> second) {

    Map<String, Set<String>> result = new HashMap<>(first);

    second.forEach(
        (service, permissions) ->
            result.merge(
                service.toUpperCase(Locale.ROOT),
                new HashSet<>(permissions),
                (existing, incoming) -> {
                  existing.addAll(incoming);
                  return existing;
                }));

    return result;
  }

  public Set<String> getPermissions(String service) {
    return registry.getOrDefault(service.toUpperCase(Locale.ROOT), Collections.emptySet());
  }

  public Set<String> getPermissions(Set<String> services) {
    Set<String> permissions = new HashSet<>(COMMON_READ_ONLY);

    for (String service : services) {
      permissions.addAll(getPermissions(service));
    }

    return Set.copyOf(permissions);
  }
}
