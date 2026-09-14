package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.deserialization.parquet.ParquetReaderService;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ParquetExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import org.springframework.stereotype.Component;

@Component
public class ParquetExportReaderFactory implements ExportReaderFactory<RawBillingRow> {

  private final ParquetReaderService readerService;

  public ParquetExportReaderFactory(ParquetReaderService readerService) {
    this.readerService = readerService;
  }

  @Override
  public ExportReader<RawBillingRow> createExportReader(
      BlobContainerClient containerClient, String blobName) {
    return new ParquetExportReader(containerClient, blobName, readerService);
  }
}
