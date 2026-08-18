package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

public interface GcpBillingIngestionStep {
  public void execute(GcpBillingContext context);
}
