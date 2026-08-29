package com.cloudsherpa.ingestion.provider.aws.permissions;

import java.util.Set;

public interface AwsPermissionsService {
  Set<String> getPermissionsRequired();
}
