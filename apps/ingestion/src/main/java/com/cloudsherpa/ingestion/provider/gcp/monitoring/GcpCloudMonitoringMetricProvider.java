package com.cloudsherpa.ingestion.provider.gcp.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.gcp.GcpClientFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GcpCloudMonitoringMetricProvider implements CloudMonitoringMetricProvider {
  private MetricServiceClient buildClient(CloudCredentials credentials) throws IOException {

    GoogleCredentials googleCredentials = GcpClientFactory.credentials(credentials);

    MetricServiceSettings settings =
        MetricServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
            .build();

    return MetricServiceClient.create(settings);
  }

  @Override
  public List<UsageRecordModel> collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request) {

    throw new UnsupportedOperationException("Unimplemented method 'collectMetrics'");
  }
}
