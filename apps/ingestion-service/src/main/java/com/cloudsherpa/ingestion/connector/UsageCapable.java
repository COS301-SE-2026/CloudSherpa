package com.cloudsherpa.ingestion.connector;

public interface UsageCapable {

  void ingestUsage(IngestionRequest request);
}
