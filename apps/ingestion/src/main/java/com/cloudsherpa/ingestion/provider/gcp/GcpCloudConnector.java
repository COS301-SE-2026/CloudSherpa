package com.cloudsherpa.ingestion.provider.gcp;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.BillingCapable;
import com.cloudsherpa.ingestion.connector.CloudConnector;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.UsageCapable;
import com.cloudsherpa.ingestion.models.BillingRecordModel;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.gcp.monitoring.CloudMonitoringMetricProvider;
import com.cloudsherpa.ingestion.provider.gcp.monitoring.GcpCloudMonitoringMetricProvider;
import com.cloudsherpa.ingestion.provider.scanner.ResourceDiscoveryService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("gcp")
public class GcpCloudConnector implements CloudConnector, UsageCapable, BillingCapable {
  private final CloudMonitoringMetricProvider metricProvider;
  private final CloudMonitoringMetricProvider mockMetricProvider;
  private final ResourceDiscoveryService discoveryService;

  public GcpCloudConnector(
      ResourceDiscoveryService resourceDiscoveryService,
      GcpCloudMonitoringMetricProvider mockMetricProvider) {
    metricProvider = new GcpCloudMonitoringMetricProvider();
    this.mockMetricProvider = mockMetricProvider;
    discoveryService = resourceDiscoveryService;
  }

  @Override
  public List<BillingRecordModel> fetchBilling(
      AccountScope accountScope, IngestionRequestEvent request) {
    return List.of(); // to be implemented
  }

  @Override
  public List<BillingRecordModel> fetchMockBilling(
      AccountScope accountScope, IngestionRequestEvent request) {
    return List.of(); // to be implemented
  }

  @Override
  public List<UsageRecordModel> fetchUsage(
      AccountScope accountScope, IngestionRequestEvent request) {
    return metricProvider.collectMetrics(accountScope, request);
  }

  @Override
  public List<UsageRecordModel> fetchMockUsage(
      AccountScope accountScope, IngestionRequestEvent request) {
    return mockMetricProvider.collectMetrics(accountScope, request);
  }

  @Override
  public String getProviderName() {
    return "GCP";
  }

  @Override
  public List<String> getAllOfferedServices() {
    return discoveryService.getServices("GCP");
  }

  @Override
  public List<ResourceDetail> getAllResources(
      CloudCredentials credentials, List<String> serviceTypes) {
    return List.of(); // to be implemented
  }

  @Override
  public boolean testConnection(CloudCredentials credentials) {
    return true; // to be implemented
  }
}
