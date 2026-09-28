package com.cloudsherpa.lib.ingestion.cost;

public record IngestionResourceGroup(
    String serviceType,
    String region,
    long resourceCount) {
}
