package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ExportReader;

public interface ExportReaderFactory<T> {

  ExportReader<T> createExportReader(BlobContainerClient containerClient, String blobName);
}
