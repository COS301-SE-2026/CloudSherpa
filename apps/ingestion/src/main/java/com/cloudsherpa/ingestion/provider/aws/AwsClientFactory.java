
package com.cloudsherpa.ingestion.provider.aws;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

public class AwsClientFactory {

  public static StaticCredentialsProvider credentialsProvider(
      CloudCredentials credentials) {

    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
        credentials.getAccessKey(),
        credentials.getSecretKey());

    return StaticCredentialsProvider.create(awsCreds);
  }

  public static Region region(CloudCredentials creds) {
    return Region.of(creds.getAwsRegion());
  }
}
