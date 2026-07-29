package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExport;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class AwsCurCleanupStep implements AwsCurIngestionPipelineStep {

  Logger logger = LoggerFactory.getLogger(AwsCurCleanupStep.class);

  @Override
  public void execute(AwsCurContext context) {
    for (BillingExport billingExport : context.getProcessingExports()) {
      logger.info("Cleaning up export '{}'", billingExport.getExportId());
      for (Path tmpFile : billingExport.getTmpPaths()) {
        try {
          Files.deleteIfExists(tmpFile);

          Path parent = tmpFile.getParent();

          // Cleans up parent dir defensively
          if (parent != null && !parent.equals(context.getAwsCurTmpDir())) {
            Files.deleteIfExists(parent);
          }
        } catch (IOException ioException) {
          throw new UncheckedIOException(
              "Attempted but failed to delete file " + tmpFile.toString(), ioException);
        }
      }
    }
  }
}
