package com.cloudsherpa.service.persistconnection.aws.service;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.service.persistconnection.aws.dto.PersistAwsConnectionRequest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AwsConnectionPersistenceService {
  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;

  public AwsConnectionPersistenceService(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository) {
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
  }

  @Transactional
  public void persistConnection(PersistAwsConnectionRequest request) {
    CloudConnection connection = getOrCreateConnection(request);
    CloudAccount account = createAccount(connection, request);
  }

  private CloudConnection getOrCreateConnection(PersistAwsConnectionRequest request) {

    List<CloudConnection> optionalConnection =
        cloudConnectionRepository.findByUserIdAndProvider(request.userId(), "AWS");

    if (optionalConnection.isEmpty()) {
      UUID connectionId = UUID.randomUUID();
      CloudConnection connection =
          new CloudConnection(
              connectionId,
              request.userId(),
              "AWS",
              "Active",
              Instant.now().atOffset(ZoneOffset.of("SAST")));
      return cloudConnectionRepository.save(connection);
    }
    return optionalConnection.getFirst();
  }

  private CloudAccount createAccount(
      CloudConnection connection, PersistAwsConnectionRequest request) {

    CloudAccount account =
        new CloudAccount(
            UUID.randomUUID(),
            connection.getId(),
            "User",
            request.displayName(),
            request.ingestionPeriod().toString(),
            Instant.now().atOffset(ZoneOffset.of("SAST")));

    return cloudAccountRepository.save(account);
  }
}
