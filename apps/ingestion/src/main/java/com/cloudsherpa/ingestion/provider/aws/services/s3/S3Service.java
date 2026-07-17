package com.cloudsherpa.ingestion.provider.aws.services.s3;

import java.nio.file.Path;
import java.util.List;
import software.amazon.awssdk.services.s3.model.S3Object;

public interface S3Service {
  public List<S3Object> listObjects(String bucketName, String prefix);

  public <T> T objectToJson(S3ObjectReference object, Class<T> jacksonConfig);

  public void downloadObject(String objectUri, Path destination);
}
