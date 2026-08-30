package com.cloudsherpa.service.persistconnection.provider.azure.dto;

import com.cloudsherpa.service.persistconnection.dto.ResourceSelectionDto;
import java.util.List;
import java.util.UUID;

public record PersistAzureConnectionRequest(
    UUID userId,
    String accountId,
    String displayName,
    Integer ingestionPeriod,
    AzureCredentialsDto credentials,
    List<ResourceSelectionDto> resources) {
  public PersistAzureConnectionRequest withUserId(UUID userId) {
    return new PersistAzureConnectionRequest(
        userId, accountId, displayName, ingestionPeriod, credentials, resources);
  }
}
