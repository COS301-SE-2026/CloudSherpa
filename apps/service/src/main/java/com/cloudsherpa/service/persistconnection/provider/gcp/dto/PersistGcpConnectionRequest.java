package com.cloudsherpa.service.persistconnection.provider.gcp.dto;

import com.cloudsherpa.service.persistconnection.dto.ResourceSelectionDto;
import java.util.List;
import java.util.UUID;

public record PersistGcpConnectionRequest(
    UUID userId,
    String accountId,
    String displayName,
    Integer ingestionPeriod,
    GcpCredentialsDto credentials,
    List<ResourceSelectionDto> resources) {
  public PersistGcpConnectionRequest withUserId(UUID userId) {
    return new PersistGcpConnectionRequest(
        userId, accountId, displayName, ingestionPeriod, credentials, resources);
  }
}
