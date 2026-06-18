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
import java.util.List;
import org.springframework.stereotype.Component;

@Component("gcp")
public class GcpCloudConnector implements CloudConnector, UsageCapable, BillingCapable {
  private final CloudMonitoringMetricProvider metricProvider;

  public GcpCloudConnector() {
    metricProvider = new GcpCloudMonitoringMetricProvider();
  }

  @Override
  public List<BillingRecordModel> fetchBilling(
      AccountScope accountScope, IngestionRequestEvent request) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'fetchBilling'");
  }

  @Override
  public List<BillingRecordModel> fetchMockBilling(
      AccountScope accountScope, IngestionRequestEvent request) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'fetchMockBilling'");
  }

  @Override
  public List<UsageRecordModel> fetchUsage(
      AccountScope accountScope, IngestionRequestEvent request) {
    return metricProvider.collectMetrics(accountScope, request);
  }

  @Override
  public List<UsageRecordModel> fetchMockUsage(
      AccountScope accountScope, IngestionRequestEvent request) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'fetchMockUsage'");
  }

  @Override
  public String getProviderName() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getProviderName'");
  }

  @Override
  public List<String> getAllOfferedServices() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getAllOfferedServices'");
  }

  @Override
  public List<ResourceDetail> getAllResources(CloudCredentials credentials) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getAllResources'");
  }

  @Override
  public boolean testConnection(CloudCredentials credentials) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'testConnection'");
  }
}
