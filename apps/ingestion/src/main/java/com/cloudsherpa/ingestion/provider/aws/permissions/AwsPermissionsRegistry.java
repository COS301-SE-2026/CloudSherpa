package com.cloudsherpa.ingestion.provider.aws.permissions;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsRegistry;
import com.cloudsherpa.ingestion.provider.scanner.ResourceDiscoveryService;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AwsPermissionsRegistry extends PermissionsRegistry {

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
    super(COMMON_READ_ONLY);
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
}
