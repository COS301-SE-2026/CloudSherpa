package com.cloudsherpa.ingestion.provider.aws;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.BillingCapable;
import com.cloudsherpa.ingestion.connector.CloudConnector;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.UsageCapable;
import com.cloudsherpa.ingestion.models.BillingRecordModel;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.aws.monitoring.AwsCloudWatchMetricProvider;
import com.cloudsherpa.ingestion.provider.aws.monitoring.CloudWatchMetricProvider;
import com.cloudsherpa.ingestion.provider.aws.monitoring.MockCloudWatchMetricProvider;
import com.cloudsherpa.ingestion.provider.aws.services.Ec2Service.AwsEc2Service;
import com.cloudsherpa.ingestion.provider.aws.services.Ec2Service.Ec2Service;
import com.cloudsherpa.ingestion.provider.aws.services.EcsService.AwsEcsService;
import com.cloudsherpa.ingestion.provider.aws.services.EcsService.EcsService;
import com.cloudsherpa.ingestion.provider.aws.services.EksService.AwsEksService;
import com.cloudsherpa.ingestion.provider.aws.services.EksService.EksService;
import com.cloudsherpa.ingestion.provider.aws.services.LambdaService.AwsLambdaService;
import com.cloudsherpa.ingestion.provider.aws.services.LambdaService.LambdaService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheClustersResponse;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.DescribeDomainRequest;
import software.amazon.awssdk.services.opensearch.model.DescribeDomainResponse;
import software.amazon.awssdk.services.opensearch.model.DomainInfo;
import software.amazon.awssdk.services.opensearch.model.DomainStatus;
import software.amazon.awssdk.services.opensearch.model.ListDomainNamesRequest;
import software.amazon.awssdk.services.opensearch.model.ListDomainNamesResponse;
import software.amazon.awssdk.services.opensearch.model.ListTagsRequest;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.redshift.RedshiftClient;

@Component("aws")
public class AwsCloudConnector implements CloudConnector, UsageCapable, BillingCapable {

  private final CloudWatchMetricProvider metricProvider;
  private final CloudWatchMetricProvider mockMetricProvider;
  private final Ec2Service ec2Service;
  private final EcsService ecsService;
  private final EksService eksService;
  private final LambdaService lambdaService;

  public AwsCloudConnector() {
    metricProvider = new AwsCloudWatchMetricProvider();
    mockMetricProvider = new MockCloudWatchMetricProvider();
    ec2Service = new AwsEc2Service();
    ecsService = new AwsEcsService();
    eksService = new AwsEksService();
    lambdaService = new AwsLambdaService();
  }

  private static final Logger log = LoggerFactory.getLogger(AwsCloudConnector.class);

  public List<ResourceDetail> getAllEc2Instances(CloudCredentials credentials) {
    return ec2Service.getAllEc2InstancesWithTags(credentials);
  }

  public List<ResourceDetail> getAllEcsClusters(CloudCredentials credentials) {
    return ecsService.getAllEcsClustersWithTags(credentials);
  }

  public List<ResourceDetail> getAllEksClusters(CloudCredentials credentials) {
    return eksService.getAllEksClustersWithTags(credentials);
  }

  public List<ResourceDetail> getAllLambdaFunctions(CloudCredentials credentials) {
    return lambdaService.getAllLambdaFunctionsWithTags(credentials);
  }

  public List<ResourceDetail> getAllRdsInstances(CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (RdsClient rds =
        RdsClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      DescribeDbInstancesResponse response = rds.describeDBInstances();

      for (DBInstance db : response.dbInstances()) {

        Map<String, String> tags =
            rds.listTagsForResource(r -> r.resourceName(db.dbInstanceArn())).tagList().stream()
                .collect(
                    Collectors.toMap(
                        software.amazon.awssdk.services.rds.model.Tag::key,
                        software.amazon.awssdk.services.rds.model.Tag::value,
                        (a, b) -> b));
        String name =
            ResourceDetail.resolveName(db.dbInstanceIdentifier(), db.dbInstanceIdentifier(), tags);
        resources.add(
            new ResourceDetail(
                db.dbInstanceIdentifier(), name, "DBInstanceIdentifier", "RDS", tags));
      }
    }

    return resources;
  }

  public List<ResourceDetail> getAllElastiCacheClusters(CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (ElastiCacheClient client =
        ElastiCacheClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      DescribeCacheClustersResponse response = client.describeCacheClusters();

      for (CacheCluster cluster : response.cacheClusters()) {

        Map<String, String> tags = Collections.emptyMap();

        if (cluster.arn() != null) {
          tags =
              client.listTagsForResource(r -> r.resourceName(cluster.arn())).tagList().stream()
                  .collect(
                      Collectors.toMap(
                          software.amazon.awssdk.services.elasticache.model.Tag::key,
                          software.amazon.awssdk.services.elasticache.model.Tag::value,
                          (a, b) -> b));
        }
        String name =
            ResourceDetail.resolveName(cluster.cacheClusterId(), cluster.cacheClusterId(), tags);
        resources.add(
            new ResourceDetail(
                cluster.cacheClusterId(), name, "CacheClusterId", "ELASTICACHE", tags));
      }
    }

    return resources;
  }

  public List<ResourceDetail> getAllOpenSearchDomains(CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (OpenSearchClient client =
        OpenSearchClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      ListDomainNamesResponse response =
          client.listDomainNames(ListDomainNamesRequest.builder().build());

      for (DomainInfo domainInfo : response.domainNames()) {

        DescribeDomainResponse domainResponse =
            client.describeDomain(
                DescribeDomainRequest.builder().domainName(domainInfo.domainName()).build());

        DomainStatus domain = domainResponse.domainStatus();

        Map<String, String> tags =
            client.listTags(ListTagsRequest.builder().arn(domain.arn()).build()).tagList().stream()
                .collect(
                    Collectors.toMap(
                        software.amazon.awssdk.services.opensearch.model.Tag::key,
                        software.amazon.awssdk.services.opensearch.model.Tag::value,
                        (a, b) -> b));
        String name = ResourceDetail.resolveName(domain.domainName(), domain.domainName(), tags);
        resources.add(
            new ResourceDetail(domain.domainName(), name, "DomainName", "OPENSEARCH", tags));
      }
    }

    return resources;
  }

  public List<ResourceDetail> getAllRedshiftClusters(CloudCredentials credentials) {

    List<ResourceDetail> resources = new ArrayList<>();

    try (RedshiftClient client =
        RedshiftClient.builder()
            .region(AwsClientFactory.region(credentials))
            .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
            .build()) {

      software.amazon.awssdk.services.redshift.model.DescribeClustersResponse response =
          client.describeClusters();

      for (software.amazon.awssdk.services.redshift.model.Cluster cluster : response.clusters()) {

        Map<String, String> tags =
            cluster.tags().stream()
                .collect(
                    Collectors.toMap(
                        software.amazon.awssdk.services.redshift.model.Tag::key,
                        software.amazon.awssdk.services.redshift.model.Tag::value,
                        (a, b) -> b));
        String name =
            ResourceDetail.resolveName(
                cluster.clusterIdentifier(), cluster.clusterIdentifier(), tags);
        resources.add(
            new ResourceDetail(
                cluster.clusterIdentifier(), name, "ClusterIdentifier", "REDSHIFT", tags));
      }
    }

    return resources;
  }

  @Override
  public List<UsageRecordModel> fetchUsage(
      AccountScope accountScope, IngestionRequestEvent request) {
    return metricProvider.collectMetrics(accountScope, request);
  }

  @Override
  public List<BillingRecordModel> fetchBilling(
      AccountScope accountScope, IngestionRequestEvent request) {
    return List.of(); // mock for now
  }

  @Override
  public List<String> getAllOfferedServices() {
    List<String> services = new ArrayList<>();
    services.add("EC2");
    services.add("ECS");
    services.add("EKS");
    services.add("Lambda");
    services.add("RDS");
    services.add("ElastiCache");
    services.add("OpenSearch");
    services.add("RedShift");

    return services;
  }

  @Override
  public List<ResourceDetail> getAllResources(CloudCredentials credentials) {
    List<ResourceDetail> resources = new ArrayList<>();

    try {
      resources.addAll(getAllEc2Instances(credentials));
    } catch (Exception e) {
      log.warn("Failed to discover EC2 resources", e);
    }

    try {
      resources.addAll(getAllEcsClusters(credentials));
    } catch (Exception e) {
      log.warn("Failed to discover ECS resources", e);
    }

    try {
      resources.addAll(getAllEksClusters(credentials));
    } catch (Exception e) {
      log.warn("Failed to discover EKS resources", e);
    }

    try {
      resources.addAll(getAllElastiCacheClusters(credentials));
    } catch (Exception e) {
      log.warn("Failed to discover ElastiCache resources", e);
    }

    try {
      resources.addAll(getAllLambdaFunctions(credentials));
    } catch (Exception e) {
      log.warn("Failed to discover Lambda resources", e);
    }

    try {
      resources.addAll(getAllOpenSearchDomains(credentials));
    } catch (Exception e) {
      log.warn("Failed to discover OpenSearch resources", e);
    }

    try {
      resources.addAll(getAllRdsInstances(credentials));
    } catch (Exception e) {
      log.warn("Failed to discover RDS resources", e);
    }

    try {
      resources.addAll(getAllRedshiftClusters(credentials));
    } catch (Exception e) {
      log.warn("Failed to discover Redshift resources", e);
    }

    return resources;
  }

  @Override
  public List<BillingRecordModel> fetchMockBilling(
      AccountScope accountScope, IngestionRequestEvent request) {
    return List.of();
  }

  @Override
  public boolean testConnection(CloudCredentials credentials) {
    CloudWatchClient client =
        CloudWatchClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.EU_NORTH_1)
            .build();

    if (credentials != null) {
      AwsBasicCredentials awsCredentials =
          AwsBasicCredentials.create(credentials.getAccessKey(), credentials.getSecretKey());
      client =
          CloudWatchClient.builder()
              .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
              .region(Region.of(credentials.getAwsRegion()))
              .build();
    }

    try {
      client.listMetrics();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String getProviderName() {
    return "AWS";
  }

  @Override
  public List<UsageRecordModel> fetchMockUsage(
      AccountScope accountScope, IngestionRequestEvent request) {
    return mockMetricProvider.collectMetrics(accountScope, request);
  }
}
