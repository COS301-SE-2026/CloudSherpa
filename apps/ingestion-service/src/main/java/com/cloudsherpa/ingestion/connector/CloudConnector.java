package com.cloudsherpa.ingestion.connector;

public interface CloudConnector {
  String getProviderName();

  void ingest(IngestionRequest request);

  boolean testConnection(CloudCredentials credentials);
}
