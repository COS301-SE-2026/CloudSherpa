package com.cloudsherpa.ingestion.provider.gcp.services.gke;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GkePermissionsService implements PermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of("Kubernetes Engine Viewer");
  }
}
