package com.cloudsherpa.service.persistconnection.aws.service;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.cloudsherpa.service.persistconnection.aws.dto.AwsCredentialsDto;
import com.cloudsherpa.service.persistconnection.aws.dto.PersistAwsConnectionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final CloudCredentialRepository cloudCredentialRepository;
  private final CredentialEncryptionService encryptionService;

  public AwsConnectionPersistenceService(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      CloudCredentialRepository cloudCredentialRepository,
      CredentialEncryptionService encryptionService) {
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.cloudCredentialRepository = cloudCredentialRepository;
    this.encryptionService = encryptionService;
  }

  @Transactional
  public void persistConnection(PersistAwsConnectionRequest request) {
    CloudConnection connection = getOrCreateConnection(request);
    CloudAccount account = createAccount(connection, request);
    createCredential(account, request.credentials());
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

  private void createCredential(CloudAccount account, AwsCredentialsDto credentials) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String json = objectMapper.writeValueAsString(credentials);
      String encrypted = encryptionService.encrypt(json);

      CloudCredential credential =
          new CloudCredential(
              UUID.randomUUID(),
              account.getId(),
              "AWS",
              "IAM_USER",
              encrypted,
              Instant.now().atOffset(ZoneOffset.of("SAST")));
      cloudCredentialRepository.save(credential);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Provided credentials could not be converted to string for connection persistence");
    }
  }
}
