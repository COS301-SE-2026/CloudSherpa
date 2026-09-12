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
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class NormalizationStep implements BillingIngestionPipelineStep<AzureBillingContext> {
  private final CsvExportReaderFactory csvReaderFactory;
  private final ParquetExportReaderFactory parquetReaderFactory;

  private Logger logger = LoggerFactory.getLogger(NormalizationStep.class);

  public NormalizationStep(
      CsvExportReaderFactory csvReaderFactory, ParquetExportReaderFactory parquetReaderFactory) {
    this.csvReaderFactory = csvReaderFactory;
    this.parquetReaderFactory = parquetReaderFactory;
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
        normalizeBlob(factory, context.getBlobContainerClient(), blob.blobName());
      }
    }
  }

  private void normalizeBlob(
      ExportReaderFactory<RawBillingRow> readerFactory,
      BlobContainerClient containerClient,
      String blobName) {
    try (ExportReader<RawBillingRow> reader =
        readerFactory.createExportReader(containerClient, blobName)) {
      // tbd
    } catch (IOException e) {
      // tbd
    } catch (ExportReaderException e) {
      logger.warn("Failed to read blob {}, SKIPPING", blobName, e);
    }
  }
}
