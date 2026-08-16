package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.connector.CloudCredentials;

public class GcpBillingContext {
  private GcpBillingConfig billingConfig;
  private String tableId;
  private CloudCredentials gcpCredentials;

  public void setGcpBillingConfig(GcpBillingConfig billingConfig) {
    this.billingConfig = billingConfig;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public void setCloudCredentials(CloudCredentials gcpCredentials) {
    this.gcpCredentials = gcpCredentials;
  }

  public GcpBillingConfig getBillingConfig() {
    return this.billingConfig;
  }

  public String getTableId() {
    return this.tableId;
  }

  public CloudCredentials getGcpCredentials() {
    return gcpCredentials;
  }
}
