package com.cloudsherpa.ingestion.provider.gcp.factory;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.google.auth.oauth2.GoogleCredentials;
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
}
