package com.cloudsherpa.ingestion.provider.gcp.services.compute;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ComputePermissionsService implements PermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of("Compute Viewer");
  }
}
