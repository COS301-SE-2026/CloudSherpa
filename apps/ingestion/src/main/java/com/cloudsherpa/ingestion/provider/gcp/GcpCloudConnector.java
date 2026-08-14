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
import com.cloudsherpa.ingestion.provider.gcp.monitoring.MockCloudMonitoringMetricProvider;
import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceDiscoveryService;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("gcp")
public class GcpCloudConnector implements CloudConnector, UsageCapable, BillingCapable {
  private final CloudMonitoringMetricProvider metricProvider;
  private final CloudMonitoringMetricProvider mockMetricProvider;
  private final GcpResourceDiscoveryService discoveryService;

  public GcpCloudConnector(
      GcpResourceDiscoveryService discoveryService,
      MockCloudMonitoringMetricProvider mockMetricProvider) {
    metricProvider = new GcpCloudMonitoringMetricProvider();
    this.mockMetricProvider = mockMetricProvider;
    this.discoveryService = discoveryService;
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
    return discoveryService.getServices();
  }

  @Override
  public List<ResourceDetail> getAllResources(
      CloudCredentials credentials, List<String> serviceTypes) {
    return discoveryService.discover(credentials, serviceTypes);
  }

  @Override
  public boolean testConnection(CloudCredentials credentials) {
    try {
      ServiceAccountCredentials accountCredentials =
          ServiceAccountCredentials.fromStream(
              new ByteArrayInputStream(
                  credentials.getServiceAccountJson().getBytes(StandardCharsets.UTF_8)));

      accountCredentials.refreshAccessToken();

      return true;

    } catch (IOException e) {
      return false;
    }
  }
}
