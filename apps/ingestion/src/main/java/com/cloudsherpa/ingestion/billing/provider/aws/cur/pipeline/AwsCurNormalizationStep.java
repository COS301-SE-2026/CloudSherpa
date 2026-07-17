package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization.AwsCurCsvNormalizerService;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.normalization.AwsCurParquetNormalizerService;
import java.nio.file.Path;
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

  Logger logger = LoggerFactory.getLogger(AwsCurNormalizationStep.class);

  public AwsCurNormalizationStep(
      AwsCurParquetNormalizerService awsCurParquetNormalizationService,
      AwsCurCsvNormalizerService awsCurCsvNormalizerService) {
    this.awsCurParquetNormalizationService = awsCurParquetNormalizationService;
    this.awsCurCsvNormalizerService = awsCurCsvNormalizerService;
  }

  @Override
  public void execute(AwsCurContext context) {
    List<AwsCurExport> processingExports = context.getProcessingExports();
    for (AwsCurExport processingExport : processingExports) {
      if (processingExport.getEncoding().equals("PARQUET")) {
        for (Path exportFile : processingExport.getTmpPaths()) {
          awsCurParquetNormalizationService.normalize(exportFile);
        }
      } else if (processingExport.getEncoding().equals("CSV")) {
        for (String dataFile : processingExport.getDataFiles()) {
          awsCurCsvNormalizerService.normalize(dataFile, context);
        }
      } else {
        throw new IllegalArgumentException("Export encoding scheme not supported");
      }
    }
  }
}
