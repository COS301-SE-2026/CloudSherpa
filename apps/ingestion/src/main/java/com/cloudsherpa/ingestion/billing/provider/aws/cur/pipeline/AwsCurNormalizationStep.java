package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization.AwsCurCsvNormalizerService;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization.AwsCurParquetNormalizerService;
import com.cloudsherpa.lib.entities.ExecutionStatusEnum;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class AwsCurNormalizationStep implements AwsCurIngestionPipelineStep {

  private final AwsCurParquetNormalizerService awsCurParquetNormalizationService;
  private final AwsCurCsvNormalizerService awsCurCsvNormalizerService;
  private final BillingExportService awsCurExportService;

  Logger logger = LoggerFactory.getLogger(AwsCurNormalizationStep.class);

  public AwsCurNormalizationStep(
      AwsCurParquetNormalizerService awsCurParquetNormalizationService,
      AwsCurCsvNormalizerService awsCurCsvNormalizerService,
      BillingExportService awsCurExportService) {
    this.awsCurParquetNormalizationService = awsCurParquetNormalizationService;
    this.awsCurCsvNormalizerService = awsCurCsvNormalizerService;
    this.awsCurExportService = awsCurExportService;
  }

  @Override
  public void execute(AwsCurContext context) {
    List<BillingExport> processingExports = context.getProcessingExports();
    for (BillingExport processingExport : processingExports) {
      awsCurExportService.transitionExportStatus(processingExport, ExecutionStatusEnum.processing);
      processingExport.setStartedAt(OffsetDateTime.now(ZoneOffset.UTC));
      if (processingExport.getEncoding().equals("PARQUET")) {
        for (Path exportFile : processingExport.getTmpPaths()) {
          awsCurParquetNormalizationService.normalize(
              exportFile, processingExport, context.getUserUuid());
        }
      } else if (processingExport.getEncoding().equals("CSV")) {
        for (String dataFile : processingExport.getDataFiles()) {
          awsCurCsvNormalizerService.normalize(dataFile, context, processingExport);
        }
      } else {
        processingExport.setExecutionStatus(ExecutionStatusEnum.failed);
        processingExport.setErrorMessage("Export encoding sheme not supported");
        awsCurExportService.updateDbExport(processingExport);
        throw new IllegalArgumentException("Export encoding scheme not supported");
      }
      processingExport.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
      processingExport.setExecutionStatus(ExecutionStatusEnum.completed);
      awsCurExportService.updateDbExport(processingExport);
    }
  }
}
