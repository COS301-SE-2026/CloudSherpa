package com.cloudsherpa.service.persistconnection.aws.dto;

import java.util.List;
import java.util.UUID;

public record PersistAwsConnectionRequest(
    UUID userId,
    String displayName,
    Integer ingestionPeriod,
    AwsCredentialsDto credentials,
    List<ResourceSelectionDto> resources) {
  public PersistAwsConnectionRequest withUserId(UUID userId) {
    return new PersistAwsConnectionRequest(
        userId, displayName, ingestionPeriod, credentials, resources);
  }
}
