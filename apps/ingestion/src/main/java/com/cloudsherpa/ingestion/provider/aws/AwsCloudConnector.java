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
import com.cloudsherpa.ingestion.provider.scanner.ResourceDiscoveryService;
import java.util.List;
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
  private final ResourceDiscoveryService discoveryService;

  public AwsCloudConnector(ResourceDiscoveryService resourceDiscoveryService) {
    metricProvider = new AwsCloudWatchMetricProvider();
    mockMetricProvider = new MockCloudWatchMetricProvider();
    discoveryService = resourceDiscoveryService;
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
          AwsBasicCredentials.create(
              credentials.getAccessKeyId(), credentials.getSecretAccessKey());
      client =
          CloudWatchClient.builder()
              .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
              .region(Region.EU_NORTH_1)
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
