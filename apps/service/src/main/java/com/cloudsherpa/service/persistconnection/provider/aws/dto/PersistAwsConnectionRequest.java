package com.cloudsherpa.service.persistconnection.provider.aws.dto;

import java.util.List;
import java.util.UUID;

import com.cloudsherpa.service.persistconnection.dto.ResourceSelectionDto;

public record PersistAwsConnectionRequest(
    UUID userId,
    String accountId,
    String displayName,
    Integer ingestionPeriod,
    AwsCredentialsDto credentials,
    List<ResourceSelectionDto> resources,
    BillingConfigDto billingConfig) {
  public PersistAwsConnectionRequest withUserId(UUID userId) {
    return new PersistAwsConnectionRequest(
        userId, accountId, displayName, ingestionPeriod, credentials, resources, billingConfig);
  }
}
