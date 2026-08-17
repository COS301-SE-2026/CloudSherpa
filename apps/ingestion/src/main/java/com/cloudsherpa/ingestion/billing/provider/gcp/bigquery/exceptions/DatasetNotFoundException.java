package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.exceptions;

public class DatasetNotFoundException extends IllegalStateException {
  public DatasetNotFoundException(String datasetId) {
    super("Dataset with ID " + datasetId + " was not found");
  }
}
