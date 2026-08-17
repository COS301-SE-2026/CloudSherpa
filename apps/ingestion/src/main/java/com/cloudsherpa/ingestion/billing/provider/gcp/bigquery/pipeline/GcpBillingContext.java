package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.google.cloud.bigquery.BigQuery;

public class GcpBillingContext {
  private GcpBillingConfig billingConfig;
  private CloudCredentials gcpCredentials;
  private BigQuery bigQueryClient;
  private String billingExportTableIdentifier;

  public void setGcpBillingConfig(GcpBillingConfig billingConfig) {
    this.billingConfig = billingConfig;
  }

  public void setCloudCredentials(CloudCredentials gcpCredentials) {
    this.gcpCredentials = gcpCredentials;
  }

  public void setBigQueryClient(BigQuery bigQueryClient) {
    this.bigQueryClient = bigQueryClient;
  }

  public void setBillingExportTableIdentifier(String billingExportTableIdentifier) {
    this.billingExportTableIdentifier = billingExportTableIdentifier;
  }

  public GcpBillingConfig getBillingConfig() {
    return billingConfig;
  }

  public CloudCredentials getGcpCredentials() {
    return gcpCredentials;
  }

  public BigQuery getBigQueryClient() {
    return bigQueryClient;
  }

  public String getBillingExportTableIdentifier() {
    return billingExportTableIdentifier;
  }
}
