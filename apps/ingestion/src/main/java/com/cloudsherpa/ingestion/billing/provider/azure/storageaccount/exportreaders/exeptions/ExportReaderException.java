package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.exeptions;

public class ExportReaderException extends RuntimeException {
  public ExportReaderException(String message, Exception e) {
    super(message, e);
  }
}
