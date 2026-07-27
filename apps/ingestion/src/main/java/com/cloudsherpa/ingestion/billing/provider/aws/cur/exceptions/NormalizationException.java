package com.cloudsherpa.ingestion.billing.provider.aws.cur.exceptions;

public class NormalizationException extends RuntimeException {
  public NormalizationException(String field, String message) {
    super(String.format("Failed to normalize field %s due to %s", field, message));
  }
}
