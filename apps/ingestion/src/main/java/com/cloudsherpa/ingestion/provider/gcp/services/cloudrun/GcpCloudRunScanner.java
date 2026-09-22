package com.cloudsherpa.ingestion.provider.gcp.services.cloudrun;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceScanner;
import com.google.cloud.asset.v1.ResourceSearchResult;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GcpCloudRunScanner implements GcpResourceScanner {

  private final GcpCloudRunService cloudRunService;
  private final CloudRunPermissionsService permissionsService;

  public GcpCloudRunScanner(
      GcpCloudRunService cloudRunService, CloudRunPermissionsService permissionsService) {
    this.cloudRunService = cloudRunService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "GCP";
  }

  @Override
  public String getServiceName() {
    return "GCP/CloudRun";
  }

  @Override
  public List<String> getAssetTypes() {
    return List.of("run.googleapis.com/Service");
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }

  @Override
  public ResourceDetail scan(ResourceSearchResult resource, CloudCredentials credentials) {
    return cloudRunService.getResourceDetail(resource, credentials);
  }
}
