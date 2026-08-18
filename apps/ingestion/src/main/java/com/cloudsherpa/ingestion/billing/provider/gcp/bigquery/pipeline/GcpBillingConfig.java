package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

// Record that holds the necessary information to be able to deterministically derive the
// bigquery dataset identifier for the billing export
public record GcpBillingConfig(String projectId, String datasetId, String billingAccountId) {}
