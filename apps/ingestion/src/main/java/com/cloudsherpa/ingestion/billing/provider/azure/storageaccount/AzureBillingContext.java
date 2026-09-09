package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount;

import com.azure.storage.blob.BlobServiceClient;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;
import java.util.UUID;

public class AzureBillingContext {

  private CloudCredentials credentials;
  private AzureBillingExportConfig exportConfig;
  private BlobServiceClient blobServiceClient;
  private UUID userId;
  private UUID configId;

  public AzureBillingContext(UUID userId, UUID configId) {
    this.userId = userId;
    this.configId = configId;
  }

  public CloudCredentials getCredentials() {
    return credentials;
  }

  public void setCredentials(CloudCredentials credentials) {
    this.credentials = credentials;
  }

  public AzureBillingExportConfig getExportConfig() {
    return exportConfig;
  }

  public void setExportConfig(AzureBillingExportConfig exportConfig) {
    this.exportConfig = exportConfig;
  }

  public BlobServiceClient getBlobServiceClient() {
    return blobServiceClient;
  }

  public void setBlobServiceClient(BlobServiceClient blobServiceClient) {
    this.blobServiceClient = blobServiceClient;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getConfigId() {
    return configId;
  }
}
