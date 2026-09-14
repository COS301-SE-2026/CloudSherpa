package com.cloudsherpa.ingestion.provider.gcp.services.storage;

public record StorageResourceIdentifier(String bucketName) {

  public static StorageResourceIdentifier fromAssetName(String assetName) {

    String prefix = "//storage.googleapis.com/";

    if (!assetName.startsWith(prefix)) {
      throw new IllegalArgumentException(
          "Unable to parse GCP Storage asset name: " + assetName);
    }

    String bucketName = assetName.substring(prefix.length());

    if (bucketName.isBlank()) {
      throw new IllegalArgumentException(
          "GCP Storage asset name does not contain a bucket name: " + assetName);
    }

    return new StorageResourceIdentifier(bucketName);
  }
}
