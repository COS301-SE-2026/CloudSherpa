package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.exceptions;

public class QueryFailedException extends IllegalStateException {
  public QueryFailedException(Exception e) {
    super("BigQuery query failed", e);
  }
}
