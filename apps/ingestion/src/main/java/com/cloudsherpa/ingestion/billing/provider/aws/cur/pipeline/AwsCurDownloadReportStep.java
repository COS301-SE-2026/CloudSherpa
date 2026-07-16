package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
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
      if (export.getEncoding().equals("PARQUET")) {
        for (String dataFileUri : export.getDataFiles()) {
          logger.info("Downloading report '{}', export '{}'", export.getExportId(), dataFileUri);
          String[] splitUri = dataFileUri.split("/");
          String filename = splitUri[splitUri.length - 2] + "/" + splitUri[splitUri.length - 1];
          Path reportPath = context.getAwsCurTmpDir().resolve(filename);
          logger.info("Downloading to '{}'", reportPath);
          try {
            Files.createDirectories(reportPath.getParent());
          } catch (FileAlreadyExistsException fileAlreadyExistsException) {
            logger.info("Directory already exists, skipping creation");
          } catch (IOException ioException) {
            throw new RuntimeException("Could not create directory", ioException);
          }

          if (Files.exists(reportPath)) {
            logger.warn("Report already exists at '{}'", reportPath);
          } else {
            context.getS3().downloadObject(dataFileUri, reportPath);
          }
        }
      }
    }
  }
}
