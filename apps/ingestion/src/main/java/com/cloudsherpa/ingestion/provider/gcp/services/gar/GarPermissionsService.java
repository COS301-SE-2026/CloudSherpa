package com.cloudsherpa.ingestion.provider.gcp.services.gar;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GarPermissionsService implements PermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of("Artifact Registry Reader");
  }
}
