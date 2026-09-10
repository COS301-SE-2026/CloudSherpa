package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders;

public interface ExportReaderFactory {

  ExportReader<?> createExportReader(String blobName);
}
