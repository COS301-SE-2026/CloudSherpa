package com.cloudsherpa.ingestion.provider.aws.permissions;

import com.cloudsherpa.ingestion.provider.scanner.ResourceDiscoveryService;
import java.util.*;

public final class AwsPermissionsRegistry {
  private static ResourceDiscoveryService discoveryRegistryProvider;

  private AwsPermissionsRegistry() {}

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

  private static final Map<String, Set<String>> REGISTRY =
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
    return REGISTRY.getOrDefault(service.toUpperCase(Locale.ROOT), Collections.emptySet());
  }
}
