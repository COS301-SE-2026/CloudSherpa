package com.cloudsherpa.ingestion.provider.gcp.services.gar;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceScanner;
import com.google.cloud.asset.v1.ResourceSearchResult;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GcpGarScanner implements GcpResourceScanner {

  private final GcpGarService garService;
  private final GarPermissionsService permissionsService;

  public GcpGarScanner(GcpGarService garService, GarPermissionsService permissionsService) {
    this.garService = garService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "GCP";
  }

  @Override
  public String getServiceName() {
    return "GCP/ArtifactRegistry";
  }

  @Override
  public List<String> getAssetTypes() {
    return List.of("artifactregistry.googleapis.com/Repository");
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }

  @Override
  public ResourceDetail scan(ResourceSearchResult resource, CloudCredentials credentials) {

    return garService.getResourceDetail(resource, credentials);
  }
}
