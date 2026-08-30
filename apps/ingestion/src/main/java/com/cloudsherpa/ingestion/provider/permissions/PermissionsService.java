package com.cloudsherpa.ingestion.provider.permissions;

import java.util.Set;

public interface PermissionsService {
  Set<String> getPermissionsRequired();
}
