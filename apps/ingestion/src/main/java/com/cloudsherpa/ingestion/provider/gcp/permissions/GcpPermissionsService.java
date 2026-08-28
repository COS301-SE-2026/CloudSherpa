package com.cloudsherpa.ingestion.provider.gcp.permissions;

import java.util.Set;

public interface GcpPermissionsService {
  Set<String> getPermissionsRequired();
}
