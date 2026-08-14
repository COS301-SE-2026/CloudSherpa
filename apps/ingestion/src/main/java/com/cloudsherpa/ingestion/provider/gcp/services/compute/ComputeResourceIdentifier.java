package com.cloudsherpa.ingestion.provider.gcp.services.compute;

public record ComputeResourceIdentifier(String projectId, String zone, String instanceName) {

  public static ComputeResourceIdentifier fromAssetName(String assetName) {

    String[] parts = assetName.split("/");

    String projectId = valueAfter(parts, "projects");
    String zone = valueAfter(parts, "zones");
    String instanceName = valueAfter(parts, "instances");

    return new ComputeResourceIdentifier(projectId, zone, instanceName);
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
