package com.cloudsherpa.ingestion.provider.azure.factory;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.metrics.MetricsClient;
import com.azure.monitor.query.metrics.MetricsClientBuilder;
import com.cloudsherpa.ingestion.connector.CloudCredentials;

public final class AzureClientFactory {
  private AzureClientFactory() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  public static MetricsClient createMetricsClient(CloudCredentials credentials) {

    ClientSecretCredential credential =
        new ClientSecretCredentialBuilder()
            .tenantId(credentials.getTenantId())
            .clientId(credentials.getClientId())
            .clientSecret(credentials.getClientSecret())
            .build();

    return new MetricsClientBuilder().credential(credential).buildClient();
  }
}
