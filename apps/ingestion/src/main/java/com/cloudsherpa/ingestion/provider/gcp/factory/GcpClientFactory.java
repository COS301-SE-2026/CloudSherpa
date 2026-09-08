package com.cloudsherpa.ingestion.provider.gcp.factory;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;
import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.cloud.container.v1.ClusterManagerSettings;
import com.google.cloud.functions.v2.FunctionServiceClient;
import com.google.cloud.functions.v2.FunctionServiceSettings;
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

  public static BigQuery createBigQueryClient(CloudCredentials credentials) throws IOException {
    ServiceAccountCredentials googleCredentials =
        (ServiceAccountCredentials) credentials(credentials);
    return BigQueryOptions.newBuilder()
        .setCredentials(googleCredentials)
        .setProjectId(googleCredentials.getProjectId())
        .build()
        .getService();
  }

  public static ClusterManagerClient createClusterManagerClient(CloudCredentials credentials)
      throws IOException {

    GoogleCredentials googleCredentials = credentials(credentials);

    ClusterManagerSettings settings =
        ClusterManagerSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
            .build();

    return ClusterManagerClient.create(settings);
  }

  public static FunctionServiceClient createFunctionsClient(CloudCredentials credentials)
      throws IOException {
    GoogleCredentials googleCredentials = credentials(credentials);

    FunctionServiceSettings settings =
        FunctionServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
            .build();

    return FunctionServiceClient.create(settings);
  }
}
