package com.cloudsherpa.ingestion.exceptions;

public class CloudMonitoringException extends RuntimeException {

  public CloudMonitoringException(String message, Throwable cause) {
    super(message, cause);
  }
}
