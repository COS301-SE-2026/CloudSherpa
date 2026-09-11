package com.cloudsherpa.ingestion.provider.gcp.services.storage;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.ResourceDetail;
import com.cloudsherpa.ingestion.provider.gcp.scanner.GcpResourceScanner;
import com.google.cloud.asset.v1.ResourceSearchResult;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GcpStorageScanner implements GcpResourceScanner {

  private final GcpStorageService storageService;
  private final StoragePermissionsService permissionsService;

  public GcpStorageScanner(
      GcpStorageService storageService,
      StoragePermissionsService permissionsService) {
    this.storageService = storageService;
    this.permissionsService = permissionsService;
  }

  @Override
  public String getProvider() {
    return "GCP";
  }

  @Override
  public String getServiceName() {
    return "GCP/Storage";
  }

  @Override
  public List<String> getAssetTypes() {
    return List.of("storage.googleapis.com/Bucket");
  }

  @Override
  public Set<String> getPermissionsRequired() {
    return permissionsService.getPermissionsRequired();
  }

  @Override
  public ResourceDetail scan(
      ResourceSearchResult resource,
      CloudCredentials credentials) {

    return storageService.getResourceDetail(resource, credentials);
  }
}
