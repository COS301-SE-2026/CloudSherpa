package com.cloudsherpa.ingestion.provider.gcp.services.functions;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceScanner;
import com.google.cloud.asset.v1.ResourceSearchResult;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GcpFunctionsScanner implements GcpResourceScanner {

  private final GcpFunctionsService functionsService;
  private final FunctionsPermissionsService permissionsService;

  public GcpFunctionsScanner(
      GcpFunctionsService functionsService, FunctionsPermissionsService permissionsService) {
    this.functionsService = functionsService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "GCP";
  }

  @Override
  public String getServiceName() {
    return "GCP/CloudFunctions";
  }

  @Override
  public List<String> getAssetTypes() {
    return List.of("cloudfunctions.googleapis.com/Function");
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }

  @Override
  public ResourceDetail scan(ResourceSearchResult resource, CloudCredentials credentials) {
    return functionsService.getResourceDetail(resource, credentials);
  }
}
