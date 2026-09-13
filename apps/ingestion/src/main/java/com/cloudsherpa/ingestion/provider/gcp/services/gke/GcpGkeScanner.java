package com.cloudsherpa.ingestion.provider.gcp.services.gke;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceScanner;
import com.google.cloud.asset.v1.ResourceSearchResult;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GcpGkeScanner implements GcpResourceScanner {

  private final GcpGkeService gkeService;
  private final GkePermissionsService permissionsService;

  public GcpGkeScanner(GcpGkeService gkeService, GkePermissionsService permissionsService) {
    this.gkeService = gkeService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "GCP";
  }

  @Override
  public String getServiceName() {
    return "GCP/GKE";
  }

  @Override
  public List<String> getAssetTypes() {
    return List.of("container.googleapis.com/Cluster");
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }

  @Override
  public ResourceDetail scan(ResourceSearchResult resource, CloudCredentials credentials) {
    return gkeService.getResourceDetail(resource, credentials);
  }
}
