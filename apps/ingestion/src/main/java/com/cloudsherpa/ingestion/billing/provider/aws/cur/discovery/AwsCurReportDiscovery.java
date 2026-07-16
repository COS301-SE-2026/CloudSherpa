package com.cloudsherpa.ingestion.billing.provider.aws.cur.discovery;

import com.cloudsherpa.ingestion.billing.provider.aws.cur.AwsCurConfig;
import com.cloudsherpa.ingestion.billing.provider.aws.cur.serialization.json.ManifestConfig;
import com.cloudsherpa.ingestion.provider.aws.services.s3.AwsS3;
import com.cloudsherpa.ingestion.provider.aws.services.s3.S3ObjectReference;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class AwsCurReportDiscovery {
  private static final Logger logger = LoggerFactory.getLogger(AwsCurReportDiscovery.class);

  private final AwsS3 s3;
  private final List<String> availablePartitions;

  // s3://<bucket-name>/<prefix>/<export-name>/metadata/<partition>/<exportname>-Manifest.json

  public AwsCurReportDiscovery() {
    this.s3 = new AwsS3();
    this.availablePartitions = new ArrayList<String>();
  }

  public void discoverCurReports(AwsCurConfig config) {
    getMetadataFiles(config);
  }

  private List<S3Object> getMetadataFiles(AwsCurConfig config) {
    List<S3Object> metadataDirectoryListing =
        this.s3.listObjects(config.getBucketName(), config.getTotalPrefix() + "/metadata");
    List<S3Object> metadataFiles =
        metadataDirectoryListing.stream().filter(object -> object.key().contains(".json")).toList();
    metadataFiles =
        metadataFiles.stream().filter(file -> !availablePartitions.contains(file.key())).toList();

    for (S3Object metadataFile : metadataFiles) {
      logger.info("New metadata files discovered '{}'", metadataFile.key());
      S3ObjectReference metadataObjectReference =
          new S3ObjectReference(config.getBucketName(), metadataFile);
      ManifestConfig manifestConfig =
          this.s3.objectToJson(metadataObjectReference, ManifestConfig.class);
      logger.info("Serialized report datafiles: '{}'", manifestConfig.getDataFiles().get(0));
    }

    return metadataFiles;
  }
}
