package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.cloudsherpa.ingestion.billing.BillingIngestionPipelineStep;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.provider.azure.factory.AzureClientFactory;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ManifestIngestion implements BillingIngestionPipelineStep<AzureBillingContext> {
  public void execute(AzureBillingContext context) {

    AzureBillingExportConfig exportConfig = context.getExportConfig();

    // Note: this does not validate that the client communicates succesfully with the storage
    // account, the list operation below does
    BlobServiceClient blobServiceClient =
        AzureClientFactory.createBlobServiceClient(
            context.getCredentials(), getBlobEndpoint(context));
    context.setBlobServiceClient(blobServiceClient);

    BlobContainerClient blobContainerClient =
        blobServiceClient.getBlobContainerClient(exportConfig.getStorageContainer());
    ListBlobsOptions listBlobsOptions =
        new ListBlobsOptions().setPrefix(getExportBlobPath(context));

    // Null timeout leaves timeout unconfigured (i.e. null is intentional)
    try {
      for (BlobItem blob : blobContainerClient.listBlobs(listBlobsOptions, null)) {}

    } catch (BlobStorageException e) {
      throw new IllegalStateException(
          "Blob listing failed with HTTP status code "
              + e.getStatusCode()
              + " and error code "
              + e.getErrorCode(),
          e);
    } catch (ClientAuthenticationException e) {
      throw new IllegalStateException("Azure authentication failed", e);
    }
  }

  private String getBlobEndpoint(AzureBillingContext context) {
    return "https://"
        + context.getExportConfig().getStorageAccountName()
        + ".blob.core.windows.net";
  }

  private String getExportBlobPath(AzureBillingContext context) {
    return context.getExportConfig().getBillingExportDirectory()
        + "/"
        + context.getExportConfig().getBillingExportName()
        + "/";
  }
}
