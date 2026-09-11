package com.cloudsherpa.ingestion.provider.gcp.services.storage;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.google.cloud.asset.v1.ResourceSearchResult;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import org.springframework.stereotype.Service;

@Service
public class GcpStorageService implements StorageService {

  @Override
  public ResourceDetail getResourceDetail(
      ResourceSearchResult resource, CloudCredentials credentials) {

    StorageResourceIdentifier identifier = StorageResourceIdentifier.fromAssetName(resource.getName());

    try {
      Storage storage = GcpClientFactory.createStorageClient(credentials);

      Bucket bucket = storage.get(identifier.bucketName());

      if (bucket == null) {
        throw new IllegalStateException(
            "GCP Cloud Storage bucket not found: " + identifier.bucketName());
      }

      return new ResourceDetail(
          bucket.getName(),
          bucket.getName(),
          "bucket_name",
          "gcs_bucket",
          bucket.getLocation(),
          bucket.getLabels());

    } catch (Exception e) {
      throw new IllegalStateException(
          "Unable to describe GCP Cloud Storage bucket " + resource.getName(), e);
    }
  }
}
