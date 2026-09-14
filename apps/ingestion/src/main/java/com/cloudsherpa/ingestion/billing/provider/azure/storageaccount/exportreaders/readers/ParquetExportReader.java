package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.deserialization.parquet.ParquetReaderService;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.exeptions.ExportReaderException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.AvroRuntimeException;
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

    List<RawBillingRow> batch = new ArrayList<>();

    while (batch.size() < maxRows) {
      GenericRecord billingRecord = reader.read();

      if (billingRecord == null) {
        break;
      }

      try {
        batch.add(readRow(billingRecord));
      } catch (AvroRuntimeException e) {
        logger.error("Bad row, SKIPPING", e);
      }
    }

    return batch;
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

  private RawBillingRow readRow(GenericRecord billingRecord) {
    String billingAccountId = billingRecord.get("billingAccountId").toString();
    LocalDate date =
        LocalDate.ofEpochDay(
            (Integer) billingRecord.get("date")); // Assumption, yet to be validated
    String consumedService = billingRecord.get("consumedService").toString();
    String meterCategory = billingRecord.get("meterCategory").toString();
    String meterSubCategory = billingRecord.get("meterSubCategory").toString();
    String resourceId = billingRecord.get("ResourceId").toString();
    String chargeType = billingRecord.get("chargeType").toString();
    String billingCurrency = billingRecord.get("billingCurrency").toString();
    BigDecimal costInPricingCurrency =
        new BigDecimal(
            billingRecord
                .get("costInPricingCurrency")
                .toString()); // Assumption, yet to be validated

    return new RawBillingRow(
        billingAccountId,
        date,
        consumedService,
        meterCategory,
        meterSubCategory,
        resourceId,
        chargeType,
        billingCurrency,
        costInPricingCurrency);
  }
}
