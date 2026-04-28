package com.cloudsherpa.ingestion.connector;

public interface BillingCapable {
  void ingestBilling(IngestionRequest request);
}
