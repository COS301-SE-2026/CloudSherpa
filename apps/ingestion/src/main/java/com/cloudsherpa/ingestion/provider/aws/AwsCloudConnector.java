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
import com.cloudsherpa.ingestion.provider.aws.services.ec2.AwsEc2Service;
import com.cloudsherpa.ingestion.provider.aws.services.ec2.Ec2Service;
import com.cloudsherpa.ingestion.provider.aws.services.ecs.AwsEcsService;
import com.cloudsherpa.ingestion.provider.aws.services.ecs.EcsService;
import com.cloudsherpa.ingestion.provider.aws.services.eks.AwsEksService;
import com.cloudsherpa.ingestion.provider.aws.services.eks.EksService;
import com.cloudsherpa.ingestion.provider.aws.services.elasticache.AwsElastiCacheService;
import com.cloudsherpa.ingestion.provider.aws.services.elasticache.ElastiCacheService;
import com.cloudsherpa.ingestion.provider.aws.services.lambda.AwsLambdaService;
import com.cloudsherpa.ingestion.provider.aws.services.lambda.LambdaService;
import com.cloudsherpa.ingestion.provider.aws.services.opensearch.AwsOpenSearchService;
import com.cloudsherpa.ingestion.provider.aws.services.opensearch.OpenSearchService;
import com.cloudsherpa.ingestion.provider.aws.services.rds.AwsRdsService;
import com.cloudsherpa.ingestion.provider.aws.services.rds.RdsService;
import com.cloudsherpa.ingestion.provider.aws.services.redshift.AwsRedshiftService;
import com.cloudsherpa.ingestion.provider.aws.services.redshift.RedshiftService;
import com.cloudsherpa.ingestion.provider.scanner.ResourceDiscoveryService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Component("aws")
public class AwsCloudConnector implements CloudConnector, UsageCapable, BillingCapable {

  private final CloudWatchMetricProvider metricProvider;
  private final CloudWatchMetricProvider mockMetricProvider;
  private final Ec2Service ec2Service;
  private final EcsService ecsService;
  private final EksService eksService;
  private final LambdaService lambdaService;
  private final RdsService rdsService;
  private final ElastiCacheService elasticacheService;
  private final OpenSearchService opensearchService;
  private final RedshiftService redshiftService;
  private final ResourceDiscoveryService discoveryService;

  public AwsCloudConnector(ResourceDiscoveryService resourceDiscoveryService) {
    metricProvider = new AwsCloudWatchMetricProvider();
    mockMetricProvider = new MockCloudWatchMetricProvider();
    ec2Service = new AwsEc2Service();
    ecsService = new AwsEcsService();
    eksService = new AwsEksService();
    lambdaService = new AwsLambdaService();
    rdsService = new AwsRdsService();
    elasticacheService = new AwsElastiCacheService();
    opensearchService = new AwsOpenSearchService();
    redshiftService = new AwsRedshiftService();
    discoveryService = resourceDiscoveryService;
  }

  private static final Logger log = LoggerFactory.getLogger(AwsCloudConnector.class);

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
    return discoveryService.getServices("AWS");
  }

  @Override
  public List<ResourceDetail> getAllResources(
      CloudCredentials credentials, List<String> serviceTypes) {
    return discoveryService.discover("AWS", serviceTypes, credentials);
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
