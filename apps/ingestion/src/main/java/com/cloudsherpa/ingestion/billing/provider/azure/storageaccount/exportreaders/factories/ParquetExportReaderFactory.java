package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.deserialization.parquet.ParquetDataConverterService;
import com.cloudsherpa.ingestion.billing.deserialization.parquet.ParquetReaderService;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ParquetExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ParquetExportReaderFactory implements ExportReaderFactory<RawBillingRow> {

  private final ParquetReaderService readerService;
  private final ParquetDataConverterService parquetDataConverterService;
  private final String tmpDirectory;

  public ParquetExportReaderFactory(
      ParquetReaderService readerService,
      ParquetDataConverterService parquetDataConverterService,
      @Value("${sherpa.billing.azure.tmp-dir}") String tmpDirectory) {
    this.readerService = readerService;
    this.tmpDirectory = tmpDirectory;
    this.parquetDataConverterService = parquetDataConverterService;
  }

  @Override
  public ExportReader<RawBillingRow> createExportReader(
      BlobContainerClient containerClient, String blobName) {
    return new ParquetExportReader(
        containerClient, blobName, readerService, parquetDataConverterService, tmpDirectory);
  }
}
