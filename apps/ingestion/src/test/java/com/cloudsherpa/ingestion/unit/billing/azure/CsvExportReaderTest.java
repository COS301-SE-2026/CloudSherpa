package com.cloudsherpa.ingestion.unit.billing.azure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.exeptions.ExportReaderException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.CsvExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import com.cloudsherpa.ingestion.provider.azure.services.blobstorage.AzureBlobReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CsvExportReaderTest {

  @Mock AzureBlobReader blobReader;
  @Mock BlobContainerClient containerClient;

  private static final String BLOB_NAME = "test-blob";

  private CsvExportReader reader;

  @Test
  void readBatchShouldNotReturnMoreThanMaxSize() throws IOException {

    String csv =
        """
                chargeId,amount
                charge-1,12.50
                charge-2,7.00
                """;

    when(blobReader.openStream(containerClient, BLOB_NAME))
        .thenAnswer(invocation -> csvInputStream(csv));

    reader = new CsvExportReader(containerClient, BLOB_NAME, blobReader);

    List<RawBillingRow> batch = reader.readBatch(1);

    assertEquals(1, batch.size());
  }

  @Test
  void readBatchShouldContinueFromNextRecordInSubsequentBatch() throws IOException {

    reader = validTestReader();

    List<RawBillingRow> batch = reader.readBatch(1);
    assertEquals(1, batch.size());

    batch = reader.readBatch(1);
    assertEquals(1, batch.size());

    batch = reader.readBatch(1);
    assertTrue(batch.isEmpty());
  }

  @Test
  void readBatchShouldHandleBatchesSmallerThanMaxRows() throws IOException {
    reader = validTestReader();

    List<RawBillingRow> batch = reader.readBatch(10);

    assertEquals(2, batch.size());
  }

  @Test
  void openStreamExceptionShouldBeHandled() {

    HttpResponse response = mock(HttpResponse.class);

    BlobStorageException exception =
        new BlobStorageException(
            "Access denied",
            response,
            "<Error><Code>AuthorizationPermissionMismatch</Code>"
                + "<Message>Access denied</Message></Error>");

    when(blobReader.openStream(containerClient, BLOB_NAME)).thenThrow(exception);

    assertThrows(
        ExportReaderException.class,
        () -> new CsvExportReader(containerClient, BLOB_NAME, blobReader));
  }

  @Test
  void brokenStreamShouldBeHandled() throws IOException {
    InputStream broken = mockBrokenStream();

    ExportReaderException thrown =
        assertThrows(
            ExportReaderException.class,
            () -> new CsvExportReader(containerClient, BLOB_NAME, blobReader));

    assertTrue(thrown.getCause() instanceof IOException);
    verify(broken).close();
  }

  private CsvExportReader validTestReader() {
    String csv =
        """
                chargeId,amount
                charge-1,12.50
                charge-2,7.00
                """;

    when(blobReader.openStream(containerClient, BLOB_NAME))
        .thenAnswer(invocation -> csvInputStream(csv));

    return new CsvExportReader(containerClient, BLOB_NAME, blobReader);
  }

  private InputStream mockBrokenStream() throws IOException {

    InputStream broken = mock(InputStream.class);
    when(broken.read()).thenThrow(new IOException());
    when(blobReader.openStream(containerClient, BLOB_NAME)).thenReturn(broken);

    return broken;
  }

  private static byte[] gzip(String text) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
      gzip.write(text.getBytes(StandardCharsets.UTF_8));
    }

    return output.toByteArray();
  }

  private static InputStream csvInputStream(String csv) throws IOException {
    return new ByteArrayInputStream(gzip(csv));
  }
}
