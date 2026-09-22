package com.cloudsherpa.ingestion.unit.billing.azure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories.CsvExportReaderFactory;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories.ParquetExportReaderFactory;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.normalization.AzureBillingNormalizer;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline.NormalizationStep;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NormalizationStepTest {
  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID EXECUTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
  private static final String RUN_ID = "normalize-run";
  private static final Integer BATCH_SIZE = 100;
  private static final String BLOB_NAME = "exports/daily/test-export/" + RUN_ID + "/part0.csv";

  @Mock CsvExportReaderFactory csvReaderFactory;
  @Mock ParquetExportReaderFactory parquetReaderFactory;
  @Mock SherpaDbPersistenceService persistenceService;
  @Mock AzureBillingNormalizer normalizer;
  @Mock BillingExportService exportService;
  @Mock ExportReader<RawBillingRow> exportReader;
  @Mock BlobContainerClient containerClient;

  @InjectMocks NormalizationStep step;

  @Test
  void shouldNotThrowWhenNoManifests() {
    AzureBillingContext context = new AzureBillingContext(USER_ID, CONFIG_ID);
    context.setManifests(Map.of());

    assertDoesNotThrow(() -> step.execute(context));
  }

  @Test
  void assertThrowsWhenExportFileFormatMissing() {
    AzureBillingContext context = constructValidContext(constructValidManifest(null));

    assertThrows(IllegalArgumentException.class, () -> step.execute(context));
  }

  @Test
  void assertThrowsWhenExportFileFormatInvalid() {
    AzureBillingContext context = constructValidContext(constructValidManifest("abc"));

    assertThrows(IllegalArgumentException.class, () -> step.execute(context));
  }

  @Test
  void assertCallNormalizeWithCsvFactory() throws IOException {
    AzureBillingContext context = constructValidContext(constructValidManifest("Csv"));
    mockCreateExportCsvReader();
    mockReadBatch();

    step.execute(context);

    verify(csvReaderFactory).createExportReader(containerClient, BLOB_NAME);
  }

  @Test
  void assertCallNormalizeWithParquetFactory() throws IOException {
    AzureBillingContext context = constructValidContext(constructValidManifest("Parquet"));
    mockCreateExportParquetReader();
    mockReadBatch();

    step.execute(context);

    verify(parquetReaderFactory).createExportReader(containerClient, BLOB_NAME);
  }

  @Test
  void assertBillingExportExecutionUpdatedSuccesfullRun() throws IOException {
    AzureBillingContext context = constructValidContext(constructValidManifest("Csv"));
    mockCreateExportCsvReader();
    mockReadBatch();

    step.execute(context);

    BillingExportExecution execution = context.getExecutions().get(EXECUTION_ID);
    assertEquals(ExecutionStatusEnum.completed, execution.getStatus());
    assertNotNull(execution.getStartedAt());
    assertNotNull(execution.getCompletedAt());
    assertNull(execution.getErrorMessage());
    assertEquals(2, execution.getRowsProcessed());
  }

  @Test
  void assertBillingExportExecutionUpdatedFailureRunIOException() throws IOException {
    AzureBillingContext context = constructValidContext(constructValidManifest("Csv"));
    mockCreateExportCsvReader();
    mockBrokenReadBatch();

    step.execute(context);

    BillingExportExecution execution = context.getExecutions().get(EXECUTION_ID);
    assertEquals(ExecutionStatusEnum.failed, execution.getStatus());
    assertNotNull(execution.getStartedAt());
    assertNotNull(execution.getCompletedAt());
    assertNotNull(execution.getErrorMessage());
    assertEquals(0, execution.getRowsProcessed());
  }

  @Test
  void assertBatchesNormalized() throws IOException {
    AzureBillingContext context = constructValidContext(constructValidManifest("Csv"));
    mockCreateExportCsvReader();
    mockReadBatch();

    BillingExportExecution execution = context.getExecutions().get(EXECUTION_ID);
    RawBillingRow firstRow = billingRow("rsrc1", "10.0");
    RawBillingRow secondRow = billingRow("rsrc2", "15.0");
    NormalizedCosts firstNormalized = new NormalizedCosts();
    firstNormalized.setResourceId("rsrc1");
    NormalizedCosts secondNormalized = new NormalizedCosts();
    secondNormalized.setResourceId("rsrc2");

    when(normalizer.normalize(firstRow, execution)).thenReturn(firstNormalized);
    when(normalizer.normalize(secondRow, execution)).thenReturn(secondNormalized);

    step.execute(context);

    InOrder processing = inOrder(normalizer, persistenceService);
    processing.verify(normalizer).normalize(firstRow, execution);
    processing.verify(persistenceService).recordCosts(List.of(firstNormalized), USER_ID);
    processing.verify(normalizer).normalize(secondRow, execution);
    processing.verify(persistenceService).recordCosts(List.of(secondNormalized), USER_ID);
    verifyNoMoreInteractions(normalizer, persistenceService);
    verify(exportReader).close();
  }

  private AzureBillingContext constructValidContext(AzureManifest manifest) {
    AzureBillingContext context = new AzureBillingContext(USER_ID, CONFIG_ID);

    Map<UUID, AzureManifest> manifests = Map.of(EXECUTION_ID, manifest);
    Map<UUID, BillingExportExecution> executions =
        Map.of(
            EXECUTION_ID,
            new BillingExportExecution(EXECUTION_ID, CONFIG_ID, ExecutionStatusEnum.pending));

    context.setManifests(manifests);
    context.setExecutions(executions);
    context.setBlobContainerClient(containerClient);

    return context;
  }

  private AzureManifest constructValidManifest(String fileFormat) {
    return new AzureManifest(
        100,
        1,
        2,
        new AzureManifest.RunInfo(
            Instant.parse("2026-09-01T00:30:00Z"),
            RUN_ID,
            Instant.parse("2026-08-01T00:00:00Z"),
            Instant.parse("2026-09-01T00:00:00Z")),
        new AzureManifest.DeliveryConfig(fileFormat),
        List.of(new AzureManifest.Partition(BLOB_NAME, 100, 2)));
  }

  private void mockCreateExportParquetReader() {
    when(parquetReaderFactory.createExportReader(containerClient, BLOB_NAME))
        .thenReturn(exportReader);
  }

  private void mockCreateExportCsvReader() {
    when(csvReaderFactory.createExportReader(containerClient, BLOB_NAME)).thenReturn(exportReader);
  }

  private void mockReadBatch() throws IOException {

    List<RawBillingRow> firstBatch = List.of(billingRow("rsrc1", "10.0"));
    List<RawBillingRow> secondBatch = List.of(billingRow("rsrc2", "15.0"));

    when(exportReader.readBatch(BATCH_SIZE)).thenReturn(firstBatch, secondBatch, List.of());
  }

  private void mockBrokenReadBatch() throws IOException {
    when(exportReader.readBatch(BATCH_SIZE)).thenThrow(new IOException());
  }

  private RawBillingRow billingRow(String resourceId, String costAmount) {
    return new RawBillingRow(
        "billing-account-1",
        LocalDate.of(2026, 9, 1),
        "Microsoft.Storage",
        "storage",
        "Blob Storage",
        resourceId,
        "Usage",
        "USD",
        new BigDecimal(costAmount));
  }
}
