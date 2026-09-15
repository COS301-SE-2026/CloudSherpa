package com.cloudsherpa.ingestion.provider.gcp.services.gar;

public record GarResourceIdentifier(String projectId, String location, String repositoryName) {

  public static GarResourceIdentifier fromAssetName(String assetName) {

    String[] parts = assetName.split("/");

    String projectId = valueAfter(parts, "projects");
    String location = valueAfter(parts, "locations");
    String repositoryName = valueAfter(parts, "repositories");

    return new GarResourceIdentifier(projectId, location, repositoryName);
  }

  private static String valueAfter(String[] parts, String key) {

    for (int i = 0; i < parts.length - 1; i++) {
      if (parts[i].equals(key)) {
        return parts[i + 1];
      }
    }

    throw new IllegalArgumentException(
        "Unable to find " + key + " in GCP Artifact Registry asset name");
  }
}
