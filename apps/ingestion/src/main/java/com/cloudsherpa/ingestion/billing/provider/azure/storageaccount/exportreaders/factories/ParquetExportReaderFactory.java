package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories;

import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ParquetExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import org.springframework.stereotype.Component;

@Component
public class ParquetExportReaderFactory implements ExportReaderFactory<RawBillingRow> {
  @Override
  public ExportReader<RawBillingRow> createExportReader(String blobName) {
    return new ParquetExportReader(blobName);
  }
}
