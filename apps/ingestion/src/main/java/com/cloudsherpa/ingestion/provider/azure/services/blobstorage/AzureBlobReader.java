package com.cloudsherpa.ingestion.provider.azure.services.blobstorage;

import com.azure.storage.blob.BlobContainerClient;
import java.io.InputStream;
import org.springframework.stereotype.Component;

@Component
public class AzureBlobReader {
  public InputStream openStream(BlobContainerClient container, String blobName) {
    return container.getBlobClient(blobName).openInputStream();
  }
}
