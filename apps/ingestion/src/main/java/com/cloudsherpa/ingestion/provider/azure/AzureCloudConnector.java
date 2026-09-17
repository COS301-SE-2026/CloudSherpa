package com.cloudsherpa.ingestion.provider.azure;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.BillingCapable;
import com.cloudsherpa.ingestion.connector.CloudConnector;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.UsageCapable;
import com.cloudsherpa.ingestion.models.BillingRecordModel;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import com.cloudsherpa.ingestion.provider.azure.monitoring.AzureCloudMonitorMetricProvider;
import com.cloudsherpa.ingestion.provider.azure.monitoring.MockCloudMonitorMetricProvider;
import com.cloudsherpa.ingestion.provider.azure.scanner.AzureResourceDiscoveryService;
import com.cloudsherpa.ingestion.provider.monitoring.CloudMonitoringMetricProvider;
import com.cloudsherpa.ingestion.service.IngestionPersistenceService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("azure")
public class AzureCloudConnector implements CloudConnector, UsageCapable, BillingCapable {

  private final CloudMonitoringMetricProvider metricProvider;
  private final CloudMonitoringMetricProvider mockMetricProvider;
  private final AzureResourceDiscoveryService discoveryService;

  public AzureCloudConnector(
      MockCloudMonitorMetricProvider mockMetricProvider,
      AzureResourceDiscoveryService discoveryService,
      IngestionPersistenceService ingestionPersistenceService) {
    metricProvider = new AzureCloudMonitorMetricProvider(ingestionPersistenceService);
    this.mockMetricProvider = mockMetricProvider;
    this.discoveryService = discoveryService;
  }

  @Override
  public void fetchUsage(
      AccountScope accountScope, IngestionRequestEvent request, Normalizer normalizer) {
    metricProvider.collectMetrics(accountScope, request, normalizer);
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
  public void fetchMockUsage(
      AccountScope accountScope, IngestionRequestEvent request, Normalizer normalizer) {
    mockMetricProvider.collectMetrics(accountScope, request, normalizer);
  }
}
