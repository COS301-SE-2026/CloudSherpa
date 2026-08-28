package com.cloudsherpa.ingestion.provider.gcp.services.compute;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceScanner;
import com.google.cloud.asset.v1.ResourceSearchResult;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GcpComputeScanner implements GcpResourceScanner {

  private final GcpComputeService computeService;
  private final ComputePermissionsService permissionsService;

  public GcpComputeScanner(
      GcpComputeService computeService, ComputePermissionsService permissionsService) {
    this.computeService = computeService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "GCP";
  }

  @Override
  public String getServiceName() {
    return "GCP/ComputeEngine";
  }

  @Override
  public List<String> getAssetTypes() {
    return List.of("compute.googleapis.com/Instance");
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }

  @Override
  public ResourceDetail scan(ResourceSearchResult resource, CloudCredentials credentials) {
    return computeService.getResourceDetail(resource, credentials);
  }
}
