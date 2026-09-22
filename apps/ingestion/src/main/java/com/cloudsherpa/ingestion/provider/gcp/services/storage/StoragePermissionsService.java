package com.cloudsherpa.ingestion.provider.gcp.services.storage;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StoragePermissionsService implements PermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of("Storage Viewer");
  }
}
