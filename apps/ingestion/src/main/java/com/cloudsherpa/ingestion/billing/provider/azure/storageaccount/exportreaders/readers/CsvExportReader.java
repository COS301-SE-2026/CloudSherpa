package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import com.cloudsherpa.ingestion.provider.azure.services.blobstorage.AzureBlobReader;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvExportReader implements ExportReader<RawBillingRow> {

  private static final Logger logger = LoggerFactory.getLogger(CsvExportReader.class);

  private final AzureBlobReader blobReader;

  private CSVParser parser;

  public CsvExportReader(
      BlobContainerClient containerClient, String blobName, AzureBlobReader blobReader) {
    this.blobReader = blobReader;

    try {
      this.parser = openCsvParser(containerClient, blobName);
    } catch (IOException e) {
      logger.error("Failed to open input stream for blob {}", blobName);
      // Throw custom exception
    }
  }

  @Override
  public List<RawBillingRow> readBatch(int maxRows) throws IOException {
    for (CSVRecord csvRecord : parser) {
      logger.info("{}", csvRecord);
    }

    return List.of();
  }

  @Override
  public void close() throws IOException {
    this.parser.close();
  }

  private CSVParser openCsvParser(BlobContainerClient containerClient, String blobName)
      throws IOException {
    Closeable resource = blobReader.openStream(containerClient, blobName);
    try {
      GZIPInputStream gzip = new GZIPInputStream((InputStream) resource);
      resource = gzip;

      Reader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));
      return CSVFormat.DEFAULT
          .builder()
          .setHeader()
          .setSkipHeaderRecord(true)
          .build()
          .parse(reader);
    } catch (RuntimeException | IOException e) {
      try {
        resource.close();
      } catch (IOException closeError) {
        e.addSuppressed(closeError);
      }
      throw e;
    }
  }
}
