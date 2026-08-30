package com.cloudsherpa.service.persistconnection.service;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.persistconnection.dto.CloudAccountDetailsResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ConnectionQueryService {
  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;
  private final ResourceRepository resourceRepository;

  public ConnectionQueryService(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      ResourceRepository resourceRepository) {
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.resourceRepository = resourceRepository;
  }

  public List<CloudAccount> getAccountConnections(UUID userId) {
    List<CloudConnection> connections = getConnection(userId);
    if (connections == null) {
      return List.of();
    }
    List<CloudAccount> accounts = new ArrayList<>();
    connections.forEach(
        connection ->
            accounts.addAll(cloudAccountRepository.findByConnectionId(connection.getId())));
    return accounts;
  }

  private List<CloudConnection> getConnection(UUID userId) {
    return cloudConnectionRepository.findByUserId(userId);
  }

  public CloudAccountDetailsResponse getAccountDetails(UUID accountId) {
    CloudAccount account =
        cloudAccountRepository
            .findById(accountId)
            .orElseThrow(
                () -> new IllegalArgumentException("No cloud account found with id " + accountId));

    return new CloudAccountDetailsResponse(
        account.getId(),
        account.getDisplayName(),
        account.getAccountType(),
        account.getConnection().getUser().getEmail(),
        account.getIngestionPeriod(),
        account.getCreatedAt());
  }

  public List<Resource> getResourcesForAccount(UUID accountId) {
    return resourceRepository.findByAccountId(accountId);
  }

  public long getResourceCountForAccount(UUID accountId) {
    return resourceRepository.countByAccountId(accountId);
  }
}
