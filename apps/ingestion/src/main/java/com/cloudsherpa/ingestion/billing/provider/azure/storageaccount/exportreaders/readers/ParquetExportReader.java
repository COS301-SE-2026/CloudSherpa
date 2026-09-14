package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.deserialization.parquet.ParquetReaderService;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.exeptions.ExportReaderException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.hadoop.ParquetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParquetExportReader implements ExportReader<RawBillingRow> {

  private final Logger logger = LoggerFactory.getLogger(ParquetExportReader.class);

  private static final Path TEMP_DIRECTORY = Paths.get("/tmp/sherpa/azure");
  private Path blobFilePath;
  private ParquetReader<GenericRecord> reader;

  public ParquetExportReader(
      BlobContainerClient containerClient, String blobName, ParquetReaderService readerService) {
    directoryExistsValidation();
    this.blobFilePath = parseBlobFilename(blobName);
    downloadBlob(containerClient, blobName);
    try {
      reader = readerService.openParquetReader(blobFilePath);
    } catch (IOException e) {
      throw new ExportReaderException(
          "Failed to open parquet file at path " + blobFilePath.toString(), e);
    }
  }

  @Override
  public List<RawBillingRow> readBatch(int maxRows) throws IOException {
    return List.of();
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  private void directoryExistsValidation() {
    try {
      Files.createDirectory(TEMP_DIRECTORY);
    } catch (IOException e) {
      String errorMessage = "Directory does not exist and failed to create directory";
      logger.error(errorMessage, e);
      throw new ExportReaderException(errorMessage, e);
    }
  }

  private void downloadBlob(BlobContainerClient containerClient, String blobName) {
    BlobClient blobClient = containerClient.getBlobClient(blobName);
    blobClient.downloadToFile(blobFilePath.toString());
  }

  private Path parseBlobFilename(String blobName) {
    String[] splitBlobName = blobName.split("/");
    return TEMP_DIRECTORY.resolve(splitBlobName[splitBlobName.length - 1]);
  }
}
