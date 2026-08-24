package com.cloudsherpa.ingestion.provider.azure.factory;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.metrics.MetricsClient;
import com.azure.monitor.query.metrics.MetricsClientBuilder;
import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import java.util.Locale;

public final class AzureClientFactory {
  private AzureClientFactory() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  public static ClientSecretCredential createCredential(CloudCredentials credentials) {

    return new ClientSecretCredentialBuilder()
        .tenantId(credentials.getTenantId())
        .clientId(credentials.getClientId())
        .clientSecret(credentials.getClientSecret())
        .build();
  }

  public static ResourceGraphManager createResourceGraphManager(CloudCredentials credentials) {

    ClientSecretCredential credential = createCredential(credentials);

    return ResourceGraphManager.authenticate(
        credential,
        new com.azure.core.management.profile.AzureProfile(
            com.azure.core.management.AzureEnvironment.AZURE));
  }

  private static String toMetricsEndpoint(String region) {
    if (region == null || region.isBlank()) {
      throw new IllegalArgumentException("Azure region is required to create the MetricsClient");
    }

    return "https://"
        + region.toLowerCase(Locale.ROOT).replace(" ", "")
        + ".metrics.monitor.azure.com";
  }

  public static MetricsClient createMetricsClient(CloudCredentials credentials, String region) {

    ClientSecretCredential credential = createCredential(credentials);
    return new MetricsClientBuilder()
        .credential(credential)
        .endpoint(toMetricsEndpoint(region))
        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
        .buildClient();
  }
}
