package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
import com.cloudsherpa.ingestion.billing.BillingIngestionPipelineStep;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model.AzureManifest;
import com.cloudsherpa.ingestion.provider.azure.services.blobstorage.AzureBlobReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class ManifestParsingStep implements BillingIngestionPipelineStep<AzureBillingContext> {

  private final Logger logger = LoggerFactory.getLogger(ManifestParsingStep.class);

  private final ObjectMapper objectMapper;

  private final AzureBlobReader blobReader;

  public ManifestParsingStep(ObjectMapper objectMapper, AzureBlobReader blobReader) {
    this.objectMapper = objectMapper;
    this.blobReader = blobReader;
  }

  public void execute(AzureBillingContext context) {
    List<AzureManifest> manifests = new ArrayList<>();

    for (BlobItem item : context.getManifestBlobItems()) {
      try {
        manifests.add(parseManifest(context.getBlobContainerClient(), item));
      } catch (IOException e) {
        logger.warn("Failed to parse manifest {}, SKIPPING", item.getName(), e);
      } catch (BlobStorageException e) {
        logger.error(
            "A Blob Storage exception occured while trying to parse manifest {}, SKIPPING",
            item.getName(),
            e);
      }
    }

    context.setManifests(manifests);
  }

  private AzureManifest parseManifest(BlobContainerClient containerClient, BlobItem item)
      throws IOException {
    try (InputStream stream = blobReader.openStream(containerClient, item.getName())) {
      return objectMapper.readValue(stream, AzureManifest.class);
    }
  }
}
