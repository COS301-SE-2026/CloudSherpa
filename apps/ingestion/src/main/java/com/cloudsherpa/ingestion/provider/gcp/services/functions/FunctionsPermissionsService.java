package com.cloudsherpa.ingestion.provider.gcp.services.functions;

import com.cloudsherpa.ingestion.provider.permissions.PermissionsService;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class FunctionsPermissionsService implements PermissionsService {

  @Override
  public Set<String> getPermissionsRequired() {
    return Set.of("Cloud Functions Viewer");
  }
}
