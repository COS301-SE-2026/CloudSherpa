package com.cloudsherpa.ingestion.provider.aws.permissions;

import java.util.*;

public final class AwsPermissionsRegistry {

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
      Map.of(
          "AWS/EC2",
          Set.of(
              "ec2:DescribeInstances",
              "ec2:DescribeInstanceStatus",
              "ec2:DescribeInstanceTypes",
              "ec2:DescribeVolumes",
              "ec2:DescribeRegions",
              "ec2:DescribeAvailabilityZones",
              "ec2:DescribeNetworkInterfaces",
              "ec2:DescribeSecurityGroups",
              "ec2:DescribeTags",
              "ec2:DescribeImages"),
          "AWS/ECS",
          Set.of(
              "ecs:ListClusters",
              "ecs:DescribeClusters",
              "ecs:ListServices",
              "ecs:DescribeServices",
              "ecs:ListTasks",
              "ecs:DescribeTasks",
              "ecs:ListTagsForResource"),
          "CONTAINERINSIGHTS",
          Set.of(
              "eks:ListClusters",
              "eks:DescribeCluster",
              "eks:ListNodegroups",
              "eks:DescribeNodegroup",
              "eks:ListTagsForResource"),
          "AWS/RDS",
          Set.of(
              "rds:DescribeDBInstances",
              "rds:DescribeDBClusters",
              "rds:DescribeDBSnapshots",
              "rds:DescribeDBSubnetGroups",
              "rds:DescribeOptionGroups",
              "rds:ListTagsForResource"),
          "AWS/DYNAMODB",
          Set.of("dynamodb:ListTables", "dynamodb:DescribeTable", "dynamodb:ListTagsOfResource"),
          "S3",
          Set.of(
              "s3:ListAllMyBuckets",
              "s3:ListBucket",
              "s3:GetBucketLocation",
              "s3:GetBucketTagging",
              "s3:GetObject"),
          "AWS/LAMBDA",
          Set.of(
              "lambda:ListFunctions",
              "lambda:GetFunction",
              "lambda:GetFunctionConfiguration",
              "lambda:ListTags"),
          "AWS/ELASTICACHE",
          Set.of(
              "elasticache:DescribeCacheClusters",
              "elasticache:DescribeReplicationGroups",
              "elasticache:DescribeSnapshots",
              "elasticache:ListTagsForResource"),
          "AWS/ES",
          Set.of(
              "opensearch:ListDomainNames",
              "opensearch:DescribeDomain",
              "opensearch:DescribeDomains",
              "opensearch:ListTags"),
          "AWS/REDSHIFT",
          Set.of(
              "redshift:DescribeClusters",
              "redshift:DescribeClusterSnapshots",
              "redshift:DescribeClusterSubnetGroups",
              "redshift:DescribeClusterParameterGroups",
              "redshift:DescribeTags"));

  public static Set<String> getPermissions(String service) {
    return REGISTRY.getOrDefault(service.toUpperCase(Locale.ROOT), Collections.emptySet());
  }
}
