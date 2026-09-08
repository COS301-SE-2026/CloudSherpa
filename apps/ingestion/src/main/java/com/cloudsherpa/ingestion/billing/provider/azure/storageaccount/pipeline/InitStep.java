package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline;

import com.cloudsherpa.ingestion.billing.BillingIngestionPipelineStep;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;

public class InitStep implements BillingIngestionPipelineStep<AzureBillingContext> {
  public void execute(AzureBillingContext context) {
    // Load config + credentials from DB + populate context

  }

  private CloudCredentials getCredentials() {
    return null;
  }

  private AzureBillingExportConfig getExportConfig() {
    return null;
  }
}
