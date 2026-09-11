package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories;

import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ExportReader;

public interface ExportReaderFactory<T> {

  ExportReader<T> createExportReader(String blobName);
}
