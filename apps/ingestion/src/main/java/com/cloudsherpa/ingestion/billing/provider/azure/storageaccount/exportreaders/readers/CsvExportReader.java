package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers;

import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import java.io.IOException;
import java.util.List;

public class CsvExportReader implements ExportReader<RawBillingRow> {

  private String blobName;
  private Integer currentRecord;

  public CsvExportReader(String blobName) {
    this.blobName = blobName;
    this.currentRecord = 0;
  }

  @Override
  public List<RawBillingRow> readBatch(int maxRows) throws IOException {
    return List.of();
  }

  @Override
  public void close() throws IOException {
    // Close CSV InputStream
  }
}
