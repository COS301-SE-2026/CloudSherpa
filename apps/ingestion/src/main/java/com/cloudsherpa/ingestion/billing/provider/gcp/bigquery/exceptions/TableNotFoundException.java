package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.exceptions;

public class TableNotFoundException extends IllegalStateException {
  public TableNotFoundException(String tableId, Exception e) {
    super("Bigquery table " + tableId + " not found", e);
  }
}
