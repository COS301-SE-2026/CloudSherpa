package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExportConfigService;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportExecution;
import com.cloudsherpa.lib.repositories.BillingExportExecutionRepository;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
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
  private final BillingExportConfigService billingExportConfigService;
  private final String awsCurTmpDir;

  public AwsCurContextInitStep(
      @Value("${sherpa.billing.aws.cur.tmp-dir}") String awsCurTmpDir,
      BillingExportExecutionRepository billingExportExecutionRepository,
      BillingExportConfigService billingExportConfigService) {
    this.awsCurTmpDir = awsCurTmpDir;
    this.billingExportExecutionRepository = billingExportExecutionRepository;
    this.billingExportConfigService = billingExportConfigService;
  }

  @Override
  public void execute(AwsCurContext context) {
    // Query from DB with what is needed
    this.logger.info("Initializing AWS CUR Ingestion Pipeline Context");
    getProcessedExports(context);

    BillingExportConfig billingExportConfig =
        billingExportConfigService.getAccountBillingExportConfig(
            UUID.fromString(context.getConfigId()));

    context.setBucketName(billingExportConfig.getBucketName().strip());
    context.setExportPrefix(billingExportConfig.getExportPrefix());
    context.setExportName(billingExportConfig.getExportName());

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
