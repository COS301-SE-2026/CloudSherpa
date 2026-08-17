package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.google.cloud.bigquery.BigQuery;

public class GcpBillingContext {
  private GcpBillingConfig billingConfig;
  private String tableId;
  private CloudCredentials gcpCredentials;
  private BigQuery bigQueryClient;

  public void setGcpBillingConfig(GcpBillingConfig billingConfig) {
    this.billingConfig = billingConfig;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public void setCloudCredentials(CloudCredentials gcpCredentials) {
    this.gcpCredentials = gcpCredentials;
  }

  public void setBigQueryClient(BigQuery bigQueryClient) {
    this.bigQueryClient = bigQueryClient;
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

  public BigQuery getBigQueryClient() {
    return bigQueryClient;
  }
}
