package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers;

import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParquetExportReader implements ExportReader<RawBillingRow> {

  private final Logger logger = LoggerFactory.getLogger(ParquetExportReader.class);

  private String blobName;

  public ParquetExportReader(String blobName) {
    this.blobName = blobName;
  }

  @Override
  public List<RawBillingRow> readBatch(int maxRows) throws IOException {
    logger.info("Blob Name {}", blobName);
    return List.of();
  }

  @Override
  public void close() throws IOException {
    // Close parquet reader
  }
}
