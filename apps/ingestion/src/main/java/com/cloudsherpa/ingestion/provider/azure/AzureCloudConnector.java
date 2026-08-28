package com.cloudsherpa.ingestion.provider.azure;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.BillingCapable;
import com.cloudsherpa.ingestion.connector.CloudConnector;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.UsageCapable;
import com.cloudsherpa.ingestion.models.BillingRecordModel;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.aws.monitoring.MockCloudWatchMetricProvider;
import com.cloudsherpa.ingestion.provider.azure.monitoring.AzureCloudMonitorMetricProvider;
import com.cloudsherpa.ingestion.provider.azure.scanner.AzureResourceDiscoveryService;
import com.cloudsherpa.ingestion.provider.monitoring.CloudMonitoringMetricProvider;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("azure")
public class AzureCloudConnector implements CloudConnector, UsageCapable, BillingCapable {

  private final CloudMonitoringMetricProvider metricProvider;
  private final CloudMonitoringMetricProvider mockMetricProvider;
  private final AzureResourceDiscoveryService discoveryService;

  public AzureCloudConnector(
      MockCloudWatchMetricProvider mockMetricProvider,
      AzureResourceDiscoveryService discoveryService) {
    metricProvider = new AzureCloudMonitorMetricProvider();
    this.mockMetricProvider = mockMetricProvider;
    this.discoveryService = discoveryService;
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
    return discoveryService.getServices();
  }

  @Override
  public List<ResourceDetail> getAllResources(
      CloudCredentials credentials, List<String> serviceTypes) {
    return discoveryService.discover(credentials, serviceTypes);
  }

  @Override
  public List<BillingRecordModel> fetchMockBilling(
      AccountScope accountScope, IngestionRequestEvent request) {
    return List.of();
  }

  @Override
  public boolean testConnection(CloudCredentials credentials) {
    return true; // unimplemented
  }

  @Override
  public String getProviderName() {
    return "Azure";
  }

  @Override
  public List<UsageRecordModel> fetchMockUsage(
      AccountScope accountScope, IngestionRequestEvent request) {
    return mockMetricProvider.collectMetrics(accountScope, request);
  }
}
