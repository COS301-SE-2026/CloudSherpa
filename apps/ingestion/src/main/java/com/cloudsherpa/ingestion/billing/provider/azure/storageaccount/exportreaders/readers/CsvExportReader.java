package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvExportReader implements ExportReader<RawBillingRow> {

  private static final Logger logger = LoggerFactory.getLogger(CsvExportReader.class);

  private String blobName;
  private Integer currentRecord;
  private BlobContainerClient containerClient;

  public CsvExportReader(BlobContainerClient containerClient, String blobName) {
    this.blobName = blobName;
    this.currentRecord = 0;
    this.containerClient = containerClient;
  }

  @Override
  public List<RawBillingRow> readBatch(int maxRows) throws IOException {
    logger.debug("CSV export reader: blob={}, currentRecord={}", blobName, currentRecord);

    // try (InputStream stream = )

    return List.of();
  }

  @Override
  public void close() throws IOException {
    // Close CSV InputStream
  }
}
