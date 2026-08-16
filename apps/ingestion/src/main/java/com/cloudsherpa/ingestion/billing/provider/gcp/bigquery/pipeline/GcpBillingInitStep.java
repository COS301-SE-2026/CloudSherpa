package com.cloudsherpa.ingestion.billing.provider.gcp.bigquery.pipeline;

import com.google.api.client.util.Value;
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
    }
  }
}
