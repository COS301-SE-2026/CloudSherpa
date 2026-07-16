package com.cloudsherpa.ingestion.provider.aws.services.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class AwsS3 {

  private final Logger logger;
  private final ObjectMapper objectMapper;

  public AwsS3() {
    this.logger = LoggerFactory.getLogger(AwsS3.class);
    this.objectMapper = new ObjectMapper();
  }

  public List<S3Object> listObjects(String bucketName, String prefix) {

    try (S3Client s3 = S3Client.builder().region(Region.EU_NORTH_1).build()) {
      logger.info("Listing objects in S3 bucket '{}' with prefix '{}'", bucketName, prefix);
      // Using local credentials for development
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

  public <T> T objectToJson(S3ObjectReference object, Class<T> jacksonConfig) {
    try (S3Client s3 = S3Client.builder().region(Region.EU_NORTH_1).build()) {
      GetObjectRequest request =
          GetObjectRequest.builder().bucket(object.bucketName()).key(object.object().key()).build();

      return jsonDeserialization(s3, object, request, jacksonConfig);

    } catch (Exception exception) {
      throw new RuntimeException(
          "Failed to build get object request for S3 object: " + object.object().key(), exception);
    }
  }

  private <T> T jsonDeserialization(
      S3Client s3, S3ObjectReference object, GetObjectRequest request, Class<T> jacksonConfig) {
    try (ResponseInputStream<GetObjectResponse> inputStream = s3.getObject(request)) {
      return objectMapper.readValue(inputStream, jacksonConfig);
    } catch (IOException exception) {
      throw new RuntimeException(
          "Failed to deserialize S3 object to json: " + object.object().key(), exception);
    }
  }
}
