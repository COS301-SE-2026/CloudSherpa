package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.billing.BillingIngestionPipelineStep;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.exeptions.ExportReaderException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories.CsvExportReaderFactory;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories.ExportReaderFactory;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories.ParquetExportReaderFactory;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest.Partition;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.normalization.AzureBillingNormalizer;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class NormalizationStep implements BillingIngestionPipelineStep<AzureBillingContext> {
  private final CsvExportReaderFactory csvReaderFactory;
  private final ParquetExportReaderFactory parquetReaderFactory;
  private final SherpaDbPersistenceService persistenceService;
  private final AzureBillingNormalizer normalizer;
  private final BillingExportService exportService;

  private static final Integer BATCH_SIZE = 100;

  private Logger logger = LoggerFactory.getLogger(NormalizationStep.class);

  public NormalizationStep(
      CsvExportReaderFactory csvReaderFactory,
      ParquetExportReaderFactory parquetReaderFactory,
      SherpaDbPersistenceService persistenceService,
      AzureBillingNormalizer normalizer,
      BillingExportService exportService) {
    this.csvReaderFactory = csvReaderFactory;
    this.parquetReaderFactory = parquetReaderFactory;
    this.persistenceService = persistenceService;
    this.normalizer = normalizer;
    this.exportService = exportService;
  }

  @Override
  public void execute(AzureBillingContext context) {
    context
        .getManifests()
        .forEach(
            (executionId, manifest) -> {
              String format = manifest.deliveryConfig().fileFormat();
              BillingExportExecution execution = context.getExecutions().get(executionId);
              execution.setStatus(ExecutionStatusEnum.processing);
              execution.setStartedAt(OffsetDateTime.now(ZoneOffset.UTC));
              exportService.updateBillingExportExecution(execution);

              if (format == null) {
                throw new IllegalArgumentException("Export file format is missing");
              }

              ExportReaderFactory<RawBillingRow> factory =
                  switch (format) {
                    case "Csv" -> csvReaderFactory;
                    case "Parquet" -> parquetReaderFactory;
                    default -> throw new IllegalArgumentException(
                        "Unsupported export file format: " + format);
                  };

              for (Partition blob : manifest.blobs()) {
                normalizeBlob(
                    factory,
                    context.getBlobContainerClient(),
                    blob.blobName(),
                    context.getUserId(),
                    execution);
              }

              if (execution.getStatus() != ExecutionStatusEnum.failed) {
                execution.setStatus(ExecutionStatusEnum.completed);
              }

              execution.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
              exportService.updateBillingExportExecution(execution);
            });
  }

  private void normalizeBlob(
      ExportReaderFactory<RawBillingRow> readerFactory,
      BlobContainerClient containerClient,
      String blobName,
      UUID userId,
      BillingExportExecution execution) {
    try (ExportReader<RawBillingRow> reader =
        readerFactory.createExportReader(containerClient, blobName)) {

      List<RawBillingRow> batch;
      Integer rowsProcessed = 0;
      execution.setRowsProcessed(rowsProcessed);
      exportService.updateBillingExportExecution(execution);
      while (!(batch = reader.readBatch(BATCH_SIZE)).isEmpty()) {
        persistenceService.recordCosts(normalizeRawBillingBatch(batch, execution), userId);
        rowsProcessed += batch.size();
        execution.setRowsProcessed(rowsProcessed);
        exportService.updateBillingExportExecution(execution);
      }

    } catch (IOException e) {
      logger.warn("Failed to read blob {}, SKIPPING", blobName, e);
      failedExecution(execution, "Failed due to IOException");
    } catch (ExportReaderException e) {
      logger.warn("Failed to read blob {}, SKIPPING", blobName, e);
      failedExecution(execution, "Failed due to ExportReaderException");
    }
  }

  private List<NormalizedCosts> normalizeRawBillingBatch(
      List<RawBillingRow> rawBillingBatch, BillingExportExecution execution) {
    return rawBillingBatch.stream().map(row -> normalizer.normalize(row, execution)).toList();
  }

  private void failedExecution(BillingExportExecution execution, String errorMessage) {
    execution.setStatus(ExecutionStatusEnum.failed);
    execution.setErrorMessage(errorMessage);
  }
}
