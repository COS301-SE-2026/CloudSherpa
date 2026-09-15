package com.cloudsherpa.ingestion.unit.billing.azure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.deserialization.parquet.ParquetReaderService;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.exeptions.ExportReaderException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ParquetExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.hadoop.ParquetReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AzureParquetExportReaderTest {

  private static final String EXPORT_BLOB_NAME = "/test-dir/test-export/test-blob.snappy.parquet";
  private static final Schema RAW_BILLING_ROW_SCHEMA =
      SchemaBuilder.record("AzureBillingRow")
          .fields()
          .requiredString("billingAccountId")
          .requiredInt("date")
          .requiredString("consumedService")
          .requiredString("meterCategory")
          .requiredString("meterSubCategory")
          .requiredString("ResourceId")
          .requiredString("chargeType")
          .requiredString("billingCurrency")
          .requiredString("costInPricingCurrency")
          .endRecord();

  @Mock BlobContainerClient blobContainerClient;
  @Mock ParquetReaderService parquetReaderService;
  @Mock ParquetReader<GenericRecord> parquetReader;
  @Mock BlobClient blobClient;

  @TempDir Path temporaryDirectory;

  @Test
  void readBatchShouldNotReturnMoreThanMaxSize() throws IOException {
    ParquetExportReader reader = createReaderWithTwoValidRecords();

    List<RawBillingRow> batch = reader.readBatch(1);

    assertEquals(1, batch.size());
  }

  @Test
  void readBatchShouldContinueFromNextRecordInSubsequentBatch() throws IOException {
    ParquetExportReader reader = createReaderWithTwoValidRecords();

    List<RawBillingRow> batch = reader.readBatch(1);
    assertEquals(1, batch.size());
    assertEquals("rsrc1", batch.get(0).resourceId());

    batch = reader.readBatch(1);
    assertEquals(1, batch.size());
    assertEquals("rsrc2", batch.get(0).resourceId());
  }

  @Test
  void readBatchShouldHandleBatchesSmallerThanMaxRows() throws IOException {
    ParquetExportReader reader = createReaderWithTwoValidRecords();

    List<RawBillingRow> batch = reader.readBatch(5);
    assertEquals(2, batch.size());
  }

  @Test
  void badRowShouldBeSkipped() throws IOException {
    ParquetExportReader reader = createReaderWithOneBadRecordAndOneValidRecord();
    List<RawBillingRow> batch = reader.readBatch(2);

    assertEquals(1, batch.size());
    assertEquals("rsrc1", batch.get(0).resourceId());
  }

  @Test
  void tmpFileNameShouldBeCorrectlyConstructed() {
    Path downloadedExportPath = temporaryDirectory.resolve("test-blob.snappy.parquet");

    stubBlobDownload(EXPORT_BLOB_NAME);

    createParquetExportReader(
        blobContainerClient, EXPORT_BLOB_NAME, parquetReaderService, temporaryDirectory.toString());

    verify(blobClient).downloadToFile(downloadedExportPath.toString());
    assertTrue(Files.exists(downloadedExportPath));
  }

  @Test
  void tmpFileShouldBeDeletedAfterReaderClosed() throws IOException {
    Path downloadedExportPath = temporaryDirectory.resolve("test-blob.snappy.parquet");

    stubBlobDownload(EXPORT_BLOB_NAME);
    stubParquetReaderService(downloadedExportPath);

    ParquetExportReader reader =
        createParquetExportReader(
            blobContainerClient,
            EXPORT_BLOB_NAME,
            parquetReaderService,
            temporaryDirectory.toString());
    reader.close();

    verify(parquetReader).close();
    assertFalse(Files.exists(downloadedExportPath));
  }

  @Test
  void tmpFileShouldBeDeletedWhenReaderFails() throws IOException {
    Path downloadedExportPath = temporaryDirectory.resolve("test-blob.snappy.parquet");

    stubBlobDownload(EXPORT_BLOB_NAME);

    when(parquetReaderService.openParquetReader(downloadedExportPath)).thenThrow(new IOException());

    String temporaryDirectoryPath = temporaryDirectory.toString();

    assertThrows(
        ExportReaderException.class,
        () ->
            createParquetExportReader(
                blobContainerClient,
                EXPORT_BLOB_NAME,
                parquetReaderService,
                temporaryDirectoryPath));

    assertFalse(Files.exists(downloadedExportPath));
  }

  private ParquetExportReader createParquetExportReader(
      BlobContainerClient blobContainerClient,
      String blobName,
      ParquetReaderService parquetReaderService,
      String tmpDirectoryString) {
    return new ParquetExportReader(
        blobContainerClient, blobName, parquetReaderService, tmpDirectoryString);
  }

  private void stubBlobDownload(String blobName) {
    when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);
    doAnswer(
            invocation -> {
              Path downloadPath = Path.of(invocation.getArgument(0, String.class));

              Files.createDirectories(downloadPath.getParent());
              Files.createFile(downloadPath);

              return null; // required for an Answer to a void method
            })
        .when(blobClient)
        .downloadToFile(anyString());
  }

  private void stubParquetReaderService(Path parquetFilePath) throws IOException {
    when(parquetReaderService.openParquetReader(parquetFilePath)).thenReturn(parquetReader);
  }

  private void stubParquetReaderService(
      Path parquetFilePath, GenericRecord firstRecord, GenericRecord secondRecord)
      throws IOException {
    when(parquetReaderService.openParquetReader(parquetFilePath)).thenReturn(parquetReader);
    when(parquetReader.read()).thenReturn(firstRecord, secondRecord, null);
  }

  private GenericRecord createBillingRecord(
      String resourceId, LocalDate date, String costInPricingCurrency) {
    GenericRecord billingRecord = new GenericData.Record(RAW_BILLING_ROW_SCHEMA);
    billingRecord.put("billingAccountId", "billing-account");
    billingRecord.put("date", Math.toIntExact(date.toEpochDay()));
    billingRecord.put("consumedService", "Microsoft.Compute");
    billingRecord.put("meterCategory", "Virtual Machines");
    billingRecord.put("meterSubCategory", "D Series");
    billingRecord.put("ResourceId", resourceId);
    billingRecord.put("chargeType", "Usage");
    billingRecord.put("billingCurrency", "USD");
    billingRecord.put("costInPricingCurrency", costInPricingCurrency);
    return billingRecord;
  }

  private ParquetExportReader createReaderWithTwoValidRecords() throws IOException {
    Path downloadedExportPath = temporaryDirectory.resolve("test-blob.snappy.parquet");
    stubBlobDownload(EXPORT_BLOB_NAME);

    GenericRecord firstRecord = createBillingRecord("rsrc1", LocalDate.of(2026, 9, 14), "12.50");
    GenericRecord secondRecord = createBillingRecord("rsrc2", LocalDate.of(2026, 9, 15), "10.50");

    stubParquetReaderService(downloadedExportPath, firstRecord, secondRecord);
    return createParquetExportReader(
        blobContainerClient, EXPORT_BLOB_NAME, parquetReaderService, temporaryDirectory.toString());
  }

  private ParquetExportReader createReaderWithOneBadRecordAndOneValidRecord() throws IOException {
    Path downloadedExportPath = temporaryDirectory.resolve("test-blob.snappy.parquet");
    stubBlobDownload(EXPORT_BLOB_NAME);

    GenericRecord badRecord = mock(GenericRecord.class);
    when(badRecord.get("billingAccountId"))
        .thenThrow(new AvroRuntimeException("Invalid billing account"));

    GenericRecord validRecord = createBillingRecord("rsrc1", LocalDate.of(2026, 9, 14), "12.50");

    stubParquetReaderService(downloadedExportPath, badRecord, validRecord);
    return createParquetExportReader(
        blobContainerClient, EXPORT_BLOB_NAME, parquetReaderService, temporaryDirectory.toString());
  }
}
