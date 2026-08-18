package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.google.cloud.bigquery.BigQuery;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class GcpBillingInitStep implements GcpBillingIngestionStep {

  @Value("${dev.gcp.billing_dev:false}")
  private boolean devConfig;

  @Value("${dev.gcp.project_id:}")
  private String devProjectId;

  @Value("${dev.gcp.dataset_id:}")
  private String devDatasetId;

  @Value("${dev.gcp.billing_account_id:}")
  private String devBillingAccountId;

  @Value("${dev.gcp.service_account_json_path:}")
  private String devServiceAccountPath;

  private final Logger logger = LoggerFactory.getLogger(GcpBillingInitStep.class);

  public void execute(GcpBillingContext context) {
    if (devConfig) {
      logger.info("GCP Billing Ingestion Dev Mode enabled ");
      if (devProjectId.isBlank() || devDatasetId.isBlank() || devBillingAccountId.isBlank()) {
        throw new IllegalStateException(
            "Dev GCP billing ingestion config enabled but configuration values are missing");
      }

      GcpBillingConfig devGcpBillingConfig =
          new GcpBillingConfig(devProjectId, devDatasetId, devBillingAccountId);

      CloudCredentials devCredentials = getGcpDevCredentials();

      context.setGcpBillingConfig(devGcpBillingConfig);
      context.setCloudCredentials(devCredentials);
      context.setBigQueryClient(getBigQueryClient(devCredentials));
    }

    // Common behavior for both dev and other configurations
    Instant queryFrom =
        Instant.now()
            .minusSeconds(
                (long) 84_000 * 3); // temporarily set queryFrom to 3 days before time of ingestion
    context.setQueryFrom(queryFrom);
  }

  private CloudCredentials getGcpDevCredentials() {
    if (!devConfig) {
      logger.error(
          "GCP dev flag set to {} but an attempt was made to obtain dev credentials", devConfig);
      throw new IllegalStateException(
          "Cannot use dev GCP credentials when devConfig flag is not set to true");
    }

    try {
      String json = Files.readString(Path.of(devServiceAccountPath));
      CloudCredentials gcpCloudCredentials = new CloudCredentials();
      gcpCloudCredentials.setServiceAccountJson(json);
      return gcpCloudCredentials;
    } catch (IOException ioException) {
      logger.error("Failed to load dev service account file at {}", devServiceAccountPath);
      throw new IllegalStateException("Could not load dev service account file", ioException);
    }
  }

  private BigQuery getBigQueryClient(CloudCredentials credentials) {
    try {
      return GcpClientFactory.createBigQueryClient(credentials);
    } catch (IOException ioException) {
      throw new IllegalStateException("Failed to obtain BigQuery client", ioException);
    }
  }
}
