package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class AwsCurDownloadReportStep implements AwsCurIngestionPipelineStep {

  Logger logger = LoggerFactory.getLogger(AwsCurDownloadReportStep.class);

  @Override
  public void execute(AwsCurContext context) {
    for (AwsCurExport export : context.getProcessingExports()) {
      for (String dataFileUri : export.getDataFiles()) {
        logger.info("Processing report '{}', export '{}'", export.getExportId(), dataFileUri);
      }
    }
  }
}
