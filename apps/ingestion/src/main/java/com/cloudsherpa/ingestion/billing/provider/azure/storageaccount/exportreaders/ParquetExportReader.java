package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders;

import java.io.IOException;
import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;

@Component
public class ParquetExportReader implements ExportReader<GenericRecord> {

  private String blobName;

  public ParquetExportReader(String blobName) {
    this.blobName = blobName;
  }

  @Override
  public List<GenericRecord> readBatch(int maxRows) throws IOException {
    return List.of();
  }

  @Override
  public void close() throws IOException {
    // Close parquet reader
  }
}
