package com.cloudsherpa.ingestion.provider.gcp.factory;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class GcpClientFactory {

  private GcpClientFactory() {}

  public static GoogleCredentials credentials(CloudCredentials credentials) throws IOException {

    return GoogleCredentials.fromStream(
        new ByteArrayInputStream(
            credentials.getServiceAccountJson().getBytes(StandardCharsets.UTF_8)));
  }

  public static InstancesClient createInstancesClient(CloudCredentials credentials)
      throws IOException {

    GoogleCredentials googleCredentials = credentials(credentials);

    InstancesSettings settings =
        InstancesSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
            .build();

    return InstancesClient.create(settings);
  }
}
