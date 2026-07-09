package com.cloudsherpa.service.persistconnection.aws.service;

import com.cloudsherpa.lib.entities.AccountTypeEnum;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.persistconnection.aws.dto.AwsCredentialsDto;
import com.cloudsherpa.service.persistconnection.aws.dto.PersistAwsConnectionRequest;
import com.cloudsherpa.service.persistconnection.aws.dto.ResourceSelectionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
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
  private final ResourceRepository resourceRepository;

  public AwsConnectionPersistenceService(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      CloudCredentialRepository cloudCredentialRepository,
      CredentialEncryptionService encryptionService,
      ResourceRepository resourceRepository) {
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.cloudCredentialRepository = cloudCredentialRepository;
    this.encryptionService = encryptionService;
    this.resourceRepository = resourceRepository;
  }

  @Transactional
  public void persistConnection(PersistAwsConnectionRequest request) {
    CloudConnection connection = getOrCreateConnection(request);
    CloudAccount account = createAccount(connection, request);
    createCredential(account, request.credentials());
    createResources(account, request.resources());
  }

  private CloudConnection getOrCreateConnection(PersistAwsConnectionRequest request) {

    List<CloudConnection> optionalConnection =
        cloudConnectionRepository.findByUserIdAndProvider(request.userId(), ProviderEnum.AWS);

    if (optionalConnection.isEmpty()) {
      UUID connectionId = UUID.randomUUID();
      CloudConnection connection =
          new CloudConnection(
              connectionId,
              request.userId(),
              ProviderEnum.AWS,
              StatusEnum.active,
              OffsetDateTime.now(ZoneOffset.UTC));
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
            AccountTypeEnum.aws_account,
            request.displayName(),
            request.ingestionPeriod().toString(),
            OffsetDateTime.now(ZoneOffset.UTC));

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
              OffsetDateTime.now(ZoneOffset.UTC));
      cloudCredentialRepository.save(credential);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize AWS credentials.", e);
    }
  }

  private void createResources(CloudAccount account, List<ResourceSelectionDto> resources) {
    List<Resource> entities =
        resources.stream()
            .map(
                r -> {
                  return new Resource(
                      UUID.randomUUID(),
                      account.getId(),
                      r.resourceType(),
                      r.resourceName(),
                      r.active() ? StatusEnum.active : StatusEnum.disabled,
                      r.tags(),
                      OffsetDateTime.now(ZoneOffset.UTC),
                      OffsetDateTime.now(ZoneOffset.UTC));
                })
            .toList();

    resourceRepository.saveAll(entities);
  }
}
