package com.cloudsherpa.service.persistconnection.aws.service;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AwsConnectionQueryService {
  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;
  private final ResourceRepository resourceRepository;

  public AwsConnectionQueryService(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      ResourceRepository resourceRepository) {
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.resourceRepository = resourceRepository;
  }

  public List<CloudAccount> getAccountConnections(UUID userId) {
    CloudConnection connection = getConnection(userId);
    if (connection == null) {
      return List.of();
    }
    return cloudAccountRepository.findByConnectionId(connection.getId());
  }

  private CloudConnection getConnection(UUID userId) {

    List<CloudConnection> connections =
        cloudConnectionRepository.findByUserIdAndProvider(userId, ProviderEnum.AWS);

    if (connections.size() > 1) {
      throw new IllegalStateException(
          "Expected exactly one AWS connection for user "
              + userId
              + " but found "
              + connections.size());
    }

    return connections.isEmpty() ? null : connections.getFirst();
  }

  public List<Resource> getResourcesForAccount(UUID accountId) {
    return resourceRepository.findByAccountId(accountId);
  }
}
