package com.cloudsherpa.ingestion.provider.aws.services.s3;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class AwsS3 {

  private static final Logger logger = LoggerFactory.getLogger(AwsS3.class);

  public List<S3Object> listObjects(String bucketName, String prefix) {

    try {
      logger.info("Listing objects in S3 bucket '{}' with prefix '{}'", bucketName, prefix);
      // Using local credentials for development
      S3Client s3 = S3Client.builder().region(Region.EU_NORTH_1).build();

      ListObjectsV2Request.Builder request = ListObjectsV2Request.builder().bucket(bucketName);

      if (prefix != null && !prefix.isBlank()) {
        request.prefix(prefix);
      }

      ListObjectsV2Response response = s3.listObjectsV2(request.build());

      logger.info("Found '{}' objects in bucket '{}'", response.contents().size(), bucketName);

      return response.contents();

    } catch (Exception exception) {
      logger.error(
          "Failed to list objects in S3 bucket '{}' with prefix '{}'",
          bucketName,
          prefix,
          exception);

      throw exception;
    }
  }
}
