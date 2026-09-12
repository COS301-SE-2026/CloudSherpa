package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline;

import com.azure.storage.blob.BlobContainerClient;
import com.cloudsherpa.ingestion.billing.BillingIngestionPipelineStep;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.exeptions.ExportReaderException;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories.CsvExportReaderFactory;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories.ExportReaderFactory;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.factories.ParquetExportReaderFactory;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.exportreaders.readers.ExportReader;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest.Partition;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.RawBillingRow;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.normalization.AzureBillingNormalizer;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class NormalizationStep implements BillingIngestionPipelineStep<AzureBillingContext> {
  private final CsvExportReaderFactory csvReaderFactory;
  private final ParquetExportReaderFactory parquetReaderFactory;
  private final SherpaDbPersistenceService persistenceService;
  private final AzureBillingNormalizer normalizer;

  private static final Integer BATCH_SIZE = 100;

  private Logger logger = LoggerFactory.getLogger(NormalizationStep.class);

  public NormalizationStep(
      CsvExportReaderFactory csvReaderFactory,
      ParquetExportReaderFactory parquetReaderFactory,
      SherpaDbPersistenceService persistenceService,
      AzureBillingNormalizer normalizer) {
    this.csvReaderFactory = csvReaderFactory;
    this.parquetReaderFactory = parquetReaderFactory;
    this.persistenceService = persistenceService;
    this.normalizer = normalizer;
  }

  @Override
  public void execute(AzureBillingContext context) {
    for (AzureManifest manifest : context.getManifests()) {
      String format = manifest.deliveryConfig().fileFormat();

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
            factory, context.getBlobContainerClient(), blob.blobName(), context.getUserId(), null);
      }
    }
  }

  private void normalizeBlob(
      ExportReaderFactory<RawBillingRow> readerFactory,
      BlobContainerClient containerClient,
      String blobName,
      UUID userId,
      BillingExportExecution execution) {
    try (ExportReader<RawBillingRow> reader =
        readerFactory.createExportReader(containerClient, blobName)) {
      persistenceService.recordCosts(
          normalizeRawBillingBatch(reader.readBatch(BATCH_SIZE), execution), userId);
    } catch (IOException e) {
      // tbd
    } catch (ExportReaderException e) {
      logger.warn("Failed to read blob {}, SKIPPING", blobName, e);
    }
  }

  private List<NormalizedCosts> normalizeRawBillingBatch(
      List<RawBillingRow> rawBillingBatch, BillingExportExecution execution) {
    return rawBillingBatch.stream().map(row -> normalizer.normalize(row, execution)).toList();
  }
}
