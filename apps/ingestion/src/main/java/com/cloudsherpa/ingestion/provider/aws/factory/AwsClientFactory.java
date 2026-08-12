package com.cloudsherpa.ingestion.provider.aws.factory;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

public final class AwsClientFactory {
  private AwsClientFactory() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  public static StaticCredentialsProvider credentialsProvider(CloudCredentials credentials) {

    AwsBasicCredentials awsCredentials =
        AwsBasicCredentials.create(credentials.getAccessKeyId(), credentials.getSecretAccessKey());

    return StaticCredentialsProvider.create(awsCredentials);
  }
}
