package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

public class GcpBillingContext {
  private GcpBillingConfig billingConfig;
  private String tableId;

  public void setGcpBillingConfig(GcpBillingConfig billingConfig) {
    this.billingConfig = billingConfig;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public GcpBillingConfig getBillingConfig() {
    return this.billingConfig;
  }

  public String getTableId() {
    return this.tableId;
  }
}
