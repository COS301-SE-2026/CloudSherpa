package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;
import java.util.List;
import java.util.UUID;

public class AzureBillingContext {

  private CloudCredentials credentials;
  private AzureBillingExportConfig exportConfig;
  private BlobServiceClient blobServiceClient;
  private BlobContainerClient blobContainerClient;
  private UUID userId;
  private UUID configId;
  private List<BlobItem> manifestBlobItems;
  private List<AzureManifest> manifests;

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

  public List<BlobItem> getManifestBlobItems() {
    return manifestBlobItems;
  }

  public void setManifestBlobItems(List<BlobItem> manifestBlobItems) {
    this.manifestBlobItems = manifestBlobItems;
  }

  public BlobContainerClient getBlobContainerClient() {
    return blobContainerClient;
  }

  public void setBlobContainerClient(BlobContainerClient blobContainerClient) {
    this.blobContainerClient = blobContainerClient;
  }

  public List<AzureManifest> getManifests() {
    return manifests;
  }

  public void setManifests(List<AzureManifest> manifests) {
    this.manifests = manifests;
  }
}
