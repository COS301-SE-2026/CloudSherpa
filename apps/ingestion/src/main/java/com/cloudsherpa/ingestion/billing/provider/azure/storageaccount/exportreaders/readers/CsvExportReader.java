package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.exeptions.ExportReaderException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import com.cloudsherpa.ingestion.provider.azure.services.blobstorage.AzureBlobReader;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvExportReader implements ExportReader<RawBillingRow> {

  private static final Logger logger = // NOSONAR keeping here for dev
      LoggerFactory.getLogger(CsvExportReader.class);

  private final AzureBlobReader blobReader;

  private CSVParser parser;

  private final Iterator<CSVRecord> records;

  public CsvExportReader(
      BlobContainerClient containerClient, String blobName, AzureBlobReader blobReader) {
    this.blobReader = blobReader;

    try {
      this.parser = openCsvParser(containerClient, blobName);
      this.records = parser.iterator();
    } catch (IOException | RuntimeException e) {
      throw new ExportReaderException("Failed to open CSV input stream for blob " + blobName, e);
    }
  }

  @Override
  public List<RawBillingRow> readBatch(int maxRows) throws IOException {

    List<RawBillingRow> batch = new ArrayList<>();

    while (batch.size() < maxRows && records.hasNext()) {
      batch.add(new RawBillingRow("tbd"));
    }

    return batch;
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

      Reader reader =
          new BufferedReader( // NOSONAR This reader is closed when this.parser.close()
              new InputStreamReader(gzip, StandardCharsets.UTF_8));
      return CSVFormat.DEFAULT
          .builder()
          .setHeader()
          .setSkipHeaderRecord(true)
          .build()
          .parse(reader);
    } catch (IOException | RuntimeException e) {
      try {
        resource.close();
      } catch (IOException closeError) {
        e.addSuppressed(closeError);
      }
      throw e;
    }
  }
}
