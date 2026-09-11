package com.cloudsherpa.ingestion.provider.gcp.services.storage;

public record StorageResourceIdentifier(String bucketName) {

  public static StorageResourceIdentifier fromAssetName(String assetName) {

    String[] parts = assetName.split("/");

    String bucketName = valueAfter(parts, "buckets");

    return new StorageResourceIdentifier(bucketName);
  }

  private static String valueAfter(String[] parts, String key) {

    for (int i = 0; i < parts.length - 1; i++) {
      if (parts[i].equals(key)) {
        return parts[i + 1];
      }
    }

    throw new IllegalArgumentException(
        "Unable to find " + key + " in GCP asset name");
  }
}
