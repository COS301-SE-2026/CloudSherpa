package com.cloudsherpa.ingestion.provider.aws.services.s3;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import java.nio.file.Path;
import java.util.List;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.S3Object;

public interface S3Service {
  public List<S3Object> listObjects(
      CloudCredentials credentials, Region region, String bucketName, String prefix);

  public <T> T objectToJson(
      CloudCredentials credentials,
      Region region,
      S3ObjectReference object,
      Class<T> jacksonConfig);

  public void downloadObject(
      CloudCredentials credentials, Region region, String objectUri, Path destination);
}
