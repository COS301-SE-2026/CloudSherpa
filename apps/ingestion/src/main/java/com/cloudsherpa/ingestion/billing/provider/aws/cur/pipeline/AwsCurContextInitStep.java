package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.repositories.BillingExportExecutionRepository;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AwsCurContextInitStep implements AwsCurIngestionPipelineStep {

  Logger logger = LoggerFactory.getLogger(AwsCurContextInitStep.class);
  private final BillingExportExecutionRepository billingExportExecutionRepository;
  private final String awsCurTmpDir;

  public AwsCurContextInitStep(
      @Value("${sherpa.billing.aws.cur.tmp-dir}") String awsCurTmpDir,
      BillingExportExecutionRepository billingExportExecutionRepository) {
    this.awsCurTmpDir = awsCurTmpDir;
    this.billingExportExecutionRepository = billingExportExecutionRepository;
  }

  @Override
  public void execute(AwsCurContext context) {
    // Query from DB with what is needed
    this.logger.info("Initializing AWS CUR Ingestion Pipeline Context");
    getProcessedExports(context);

    context.setBucketName("test-bucket-564907680089-eu-north-1-an");
    context.setExportPrefix("exports");
    context.setExportName("CloudSherpaCsvExport");

    context.setAwsCurTmpDir(Path.of(awsCurTmpDir));

    this.logger.info("Initialized AWS CUR Ingestion Pipeline Context");
  }

  // This could problably be a bean and not be run on each export
  private void getProcessedExports(AwsCurContext context) {
    this.logger.info("Querying DB to get processed executions");

    List<BillingExportExecution> processedExportExecutions =
        billingExportExecutionRepository.findAll();

    for (BillingExportExecution processedExportExecution : processedExportExecutions) {
      context.addProcessedExport(processedExportExecution.getId().toString());
    }
  }
}
