package com.cloudsherpa.ingestion.provider.gcp.services.functions;

public record FunctionsResourceIdentifier(String projectId, String location, String functionName) {

  public static FunctionsResourceIdentifier fromAssetName(String assetName) {

    String[] parts = assetName.split("/");

    String projectId = valueAfter(parts, "projects");
    String location = valueAfter(parts, "locations");
    String functionName = valueAfter(parts, "functions");

    return new FunctionsResourceIdentifier(projectId, location, functionName);
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
