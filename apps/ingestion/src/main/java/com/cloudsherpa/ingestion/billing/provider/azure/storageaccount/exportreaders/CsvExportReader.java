package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders;

import java.io.IOException;
import java.util.List;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CsvExportReader implements ExportReader<CSVRecord> {

  private String blobName;
  private Integer currentRecord;

  public CsvExportReader(String blobName) {
    this.blobName = blobName;
    this.currentRecord = 0;
  }

  @Override
  public List<CSVRecord> readBatch(int maxRows) throws IOException {
    return List.of();
  }

  @Override
  public void close() throws IOException {
    // Close CSV InputStream
  }
}
