package com.cloudsherpa.ingestion.provider.gcp.services.cloudrun;

public record CloudRunResourceIdentifier(String projectId, String location, String serviceName) {

  public static CloudRunResourceIdentifier fromAssetName(String assetName) {

    String[] parts = assetName.split("/");

    String projectId = valueAfter(parts, "projects");
    String location = valueAfter(parts, "locations");
    String serviceName = valueAfter(parts, "services");

    return new CloudRunResourceIdentifier(projectId, location, serviceName);
  }

  private static String valueAfter(String[] parts, String key) {

    for (int i = 0; i < parts.length - 1; i++) {
      if (parts[i].equals(key)) {
        return parts[i + 1];
      }
    }

    throw new IllegalArgumentException("Unable to find " + key + " in GCP asset name");
  }
}
