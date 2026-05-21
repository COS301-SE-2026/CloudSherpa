package com.cloudsherpa.ingestion.connector;

public interface CloudConnector {
  String getProviderName();

  boolean testConnection(CloudCredentials credentials);
}
