package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers;

import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import java.io.IOException;
import java.util.List;

public class ParquetExportReader implements ExportReader<RawBillingRow> {

  private String blobName;

  public ParquetExportReader(String blobName) {
    this.blobName = blobName;
  }

  @Override
  public List<RawBillingRow> readBatch(int maxRows) throws IOException {
    return List.of();
  }

  @Override
  public void close() throws IOException {
    // Close parquet reader
  }
}
