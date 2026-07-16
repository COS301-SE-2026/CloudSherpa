package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import java.nio.file.Path;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AwsCurContextInitStep implements AwsCurIngestionPipelineStep {

  Logger logger = LoggerFactory.getLogger(AwsCurContextInitStep.class);

  @Override
  public void execute(AwsCurContext context) {
    // Query from DB with what is needed
    this.logger.info("Initializing AWS CUR Ingestion Pipeline Context");
    // MOCK for now, need to query processed export ids from DB still
    context.setProcessedExports(new ArrayList<>());

    // Hardcoded for now, need to fetch from DB
    context.setBucketName("test-bucket-564907680089-eu-north-1-an");
    context.setExportPrefix("/exports");
    context.setExportName("CloudSherpaExport");
    context.setAwsCurTmpDir(Path.of("/tmp/sherpa/aws/cur/")); // will replace with env var

    this.logger.info("Initialized AWS CUR Ingestion Pipeline Context");
  }
}
