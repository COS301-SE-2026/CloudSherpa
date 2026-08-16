package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

public class GcpBillingContext {
  private GcpBillingConfig billingConfig;

  public void setGcpBillingConfig(GcpBillingConfig billingConfig) {
    this.billingConfig = billingConfig;
  }

  public GcpBillingConfig getBillingConfig() {
    return this.billingConfig;
  }
}
