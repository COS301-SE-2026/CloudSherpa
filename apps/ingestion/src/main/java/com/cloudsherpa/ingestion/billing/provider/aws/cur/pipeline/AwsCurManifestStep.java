package com.cloudsherpa.ingestion.billing.provider.aws.cur.pipeline;

import com.cloudsherpa.ingestion.billing.BillingExport;
import com.cloudsherpa.ingestion.billing.BillingExportService;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.deserialization.json.ManifestConfig;
import com.cloudsherpa.ingestion.provider.aws.services.s3.S3ObjectReference;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
@Order(2)
public class AwsCurManifestStep implements AwsCurIngestionPipelineStep {
  Logger logger = LoggerFactory.getLogger(AwsCurManifestStep.class);
  private final BillingExportService awsCurExportService;

  public AwsCurManifestStep(BillingExportService awsCurExportService) {
    this.awsCurExportService = awsCurExportService;
  }

  @Override
  public void execute(AwsCurContext context) {
    List<S3Object> metadataDirectoryListing =
        context
            .getS3()
            .listObjects(
                context.getCredentials(),
                context.getBucketName(),
                context.getExportPrefix() + "/" + context.getExportName() + "/metadata");

    List<S3Object> metadataFiles =
        metadataDirectoryListing.stream().filter(object -> object.key().contains(".json")).toList();

    for (S3Object metadataFile : metadataFiles) {
      logger.info("New metadata files discovered '{}'", metadataFile.key());
      S3ObjectReference metadataObjectReference =
          new S3ObjectReference(context.getBucketName(), metadataFile);
      ManifestConfig manifestConfig =
          context
              .getS3()
              .objectToJson(
                  context.getCredentials(), metadataObjectReference, ManifestConfig.class);
      if (!context.getProcessedExports().contains(manifestConfig.getExecutionId())) {

        BillingExport newExport =
            awsCurExportService.initializeExport(
                manifestConfig.getExecutionId(),
                context.getConfigId(),
                manifestConfig.getDataFiles());

        if (manifestConfig.getDataFiles().get(0).contains(".parquet")) {
          newExport.setEncoding("PARQUET");
        } else if (manifestConfig.getDataFiles().get(0).contains(".csv.gz")) {
          newExport.setEncoding("CSV");
        } else {
          newExport.setEncoding("UNKNOWN");
        }

        context.getProcessingExports().add(newExport);
      }
    }
  }
}
