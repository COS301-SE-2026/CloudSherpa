package com.cloudsherpa.ingestion.provider.gcp.resource;

public record GcpResourceIdentifier(String projectId, String location, String resourceName) {}
