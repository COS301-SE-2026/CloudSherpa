package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.TableResult;
import java.time.Instant;
import java.util.UUID;

public class GcpBillingContext {
  private final UUID userId;
  private final UUID configId;
  private GcpBillingConfig billingConfig;
  private CloudCredentials gcpCredentials;
  private BigQuery bigQueryClient;
  private String billingExportTableIdentifier;
  private String fullyQualifiedExportTableIdentifier;
  private Instant queryFrom;
  private TableResult tableResult;

  public GcpBillingContext(UUID userId, UUID configId) {
    this.userId = userId;
    this.configId = configId;
  }

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

  public void setFullyQualifiedExportTableIdentifier(String fullyQualifiedExportTableIdentifier) {
    this.fullyQualifiedExportTableIdentifier = fullyQualifiedExportTableIdentifier;
  }

  public void setQueryFrom(Instant queryFrom) {
    this.queryFrom = queryFrom;
  }

  public void setTableResult(TableResult tableResult) {
    this.tableResult = tableResult;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getConfigId() {
    return configId;
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

  public String getFullyQualifiedExportTableIdentifier() {
    return fullyQualifiedExportTableIdentifier;
  }

  public Instant getQueryFrom() {
    return queryFrom;
  }

  public TableResult getTableResult() {
    return tableResult;
  }
}
