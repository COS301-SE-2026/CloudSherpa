package com.cloudsherpa.ingestion.provider.aws.services.s3;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class AwsS3 implements S3Service {

  private final Logger logger;
  private final ObjectMapper objectMapper;

  public AwsS3() {
    this.logger = LoggerFactory.getLogger(AwsS3.class);
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public List<S3Object> listObjects(
      CloudCredentials credentials, String bucketName, String prefix) {

    try (S3Client s3 = buildClient(credentials)) {
      logger.info("Listing objects in S3 bucket '{}' with prefix '{}'", bucketName, prefix);
      ListObjectsV2Request.Builder request = ListObjectsV2Request.builder().bucket(bucketName);

      if (prefix != null && !prefix.isBlank()) {
        request.prefix(prefix);
      }

      ListObjectsV2Response response = s3.listObjectsV2(request.build());

      logger.info("Found '{}' objects in bucket '{}'", response.contents().size(), bucketName);
      for (S3Object object : response.contents()) {
        logger.info(object.key());
      }

      return response.contents();
    }
  }

  @Override
  public <T> T objectToJson(
      CloudCredentials credentials, S3ObjectReference object, Class<T> jacksonConfig) {
    try (S3Client s3 = buildClient(credentials)) {
      logger.info(
          "Deserializing object to json: Bucket '{}', Key '{}'",
          object.bucketName(),
          object.object().key());
      GetObjectRequest request =
          GetObjectRequest.builder().bucket(object.bucketName()).key(object.object().key()).build();
      return jsonDeserialization(s3, object, request, jacksonConfig);
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

  @Override
  public void downloadObject(CloudCredentials credentials, String objectUri, Path destination) {

    try (S3Client s3 = buildClient(credentials)) {

      S3ObjectUriReference s3Uri = uriHelper(s3, objectUri);
      GetObjectRequest request =
          GetObjectRequest.builder().bucket(s3Uri.bucketName()).key(s3Uri.key()).build();

      logger.info("Downloading S3 object: '{}'", s3Uri.key());
      s3.getObject(request, destination);
    }
  }

  public S3ObjectUriReference uriHelper(S3Client s3, String objectUri) {
    S3Uri parsedUri = s3.utilities().parseUri(URI.create(objectUri));

    String bucket =
        parsedUri.bucket().orElseThrow(() -> new IllegalArgumentException("S3 URI has no bucket"));

    String key =
        parsedUri.key().orElseThrow(() -> new IllegalArgumentException("S3 URI has no key"));

    return new S3ObjectUriReference(bucket, key);
  }

  private S3Client buildClient(CloudCredentials credentials) {
    return S3Client.builder()
        .region(AwsClientFactory.region(credentials))
        .credentialsProvider(AwsClientFactory.credentialsProvider(credentials))
        .build();
  }
}
