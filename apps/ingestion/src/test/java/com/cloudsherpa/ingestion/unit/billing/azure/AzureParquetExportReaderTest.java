package com.cloudsherpa.ingestion.unit.billing.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.deserialization.parquet.ParquetReaderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AzureParquetExportReaderTest {

  @Mock BlobContainerClient containerClient;
  @Mock ParquetReaderService readerService;
  @Mock BlobClient blobClient;

  @Test
  void readBatchShouldNotReturnMoreThanMaxSize() {}

  @Test
  void readBatchShouldContinueFromNextRecordInSubsequentBatch() {}

  @Test
  void readBatchShouldHandleBatchesSmallerThanMaxRows() {}

  @Test
  void badRowShouldBeSkipped() {}

  @Test
  void tmpFileNameShouldBeCorrectlyConstructed() {}

  @Test
  void tmpFileShouldBeDeletedAfterSuccesfulRun() {}

  @Test
  void tmpFileShouldBeDeletedWhenReaderFails() {}

  // Arrange questions
  // What are we going to do with the file?
  //

  private void mockDownloadBlob() {}
}
