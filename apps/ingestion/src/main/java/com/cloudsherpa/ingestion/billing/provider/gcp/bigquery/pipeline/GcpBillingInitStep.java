package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.google.api.client.util.Value;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class GcpBillingInitStep implements GcpBillingIngestionStep {

  @Value("${dev.gcp.billing_dev}")
  private boolean devConfig;

  @Value("${dev.gcp.project_id}")
  private String devProjectId;

  @Value("${dev.gcp.dataset_id}")
  private String devDatasetId;

  @Value("${dev.gcp.billing_account_id}")
  private String devBillingAccountId;

  @Value("${dev.gcp.service_account_json_path}")
  private String devServiceAccountPath;

  private final Logger logger = LoggerFactory.getLogger(GcpBillingInitStep.class);

  public void execute(GcpBillingContext context) {
    if (devConfig) {
      logger.info("GCP Billing Ingestion Dev Mode enabled ");
      if (devProjectId == null || devDatasetId == null || devBillingAccountId == null) {
        throw new IllegalStateException(
            "Dev GCP billing ingestion config enabled but configuration values are missing");
      }

      GcpBillingConfig devGcpBillingConfig =
          new GcpBillingConfig(devProjectId, devDatasetId, devBillingAccountId);

      context.setGcpBillingConfig(devGcpBillingConfig);
      context.setCloudCredentials(getGcpDevCredentials());
    }
  }

  private CloudCredentials getGcpDevCredentials() {
    if (!devConfig) {
      throw new IllegalStateException(
          "Cannot use dev GCP credentials when devConfig flag is not set to true");
    }

    try {
      String json = Files.readString(Path.of(devServiceAccountPath));
      CloudCredentials gcpCloudCredentials = new CloudCredentials();
      gcpCloudCredentials.setServiceAccountJson(json);
      return gcpCloudCredentials;
    } catch (IOException ioException) {
      throw new IllegalStateException("Could not load dev service account file", ioException);
    }
  }
}
