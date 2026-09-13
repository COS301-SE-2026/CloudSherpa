package com.cloudsherpa.ingestion.provider.gcp.services.cloudrun;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CloudRunPermissionsService implements PermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of("Cloud Run Viewer");
  }
}
