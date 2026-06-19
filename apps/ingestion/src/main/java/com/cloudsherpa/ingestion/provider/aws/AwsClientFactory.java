package com.cloudsherpa.ingestion.provider.aws;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

public final class AwsClientFactory {
  private AwsClientFactory() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  public static StaticCredentialsProvider credentialsProvider(CloudCredentials credentials) {

    AwsBasicCredentials awsCredentials =
        AwsBasicCredentials.create(credentials.getAccessKey(), credentials.getSecretKey());

    return StaticCredentialsProvider.create(awsCredentials);
  }

  public static Region region(CloudCredentials credentials) {
    return Region.of(credentials.getAwsRegion());
  }
}
