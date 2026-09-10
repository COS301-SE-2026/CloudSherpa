package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.CsvExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import org.springframework.stereotype.Component;

@Component
public class CsvExportReaderFactory implements ExportReaderFactory<RawBillingRow> {
  @Override
  public ExportReader<RawBillingRow> createExportReader(
      BlobContainerClient containerClient, String blobName) {
    return new CsvExportReader(containerClient, blobName);
  }
}
