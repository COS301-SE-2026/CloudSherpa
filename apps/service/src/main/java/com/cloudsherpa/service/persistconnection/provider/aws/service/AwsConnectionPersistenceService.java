package com.cloudsherpa.service.persistconnection.provider.aws.service;

import com.cloudsherpa.lib.entities.AccountTypeEnum;
import com.cloudsherpa.lib.entities.AwsBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.AwsBillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.service.ResourceRegistryService;
import com.cloudsherpa.service.persistconnection.provider.aws.dto.AwsCredentialsDto;
import com.cloudsherpa.service.persistconnection.provider.aws.dto.BillingConfigDto;
import com.cloudsherpa.service.persistconnection.provider.aws.dto.PersistAwsConnectionRequest;
import com.cloudsherpa.service.persistconnection.service.ConnectionPersistenceService;
import com.cloudsherpa.service.persistconnection.service.CredentialEncryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AwsConnectionPersistenceService extends ConnectionPersistenceService {
  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;
  private final CloudCredentialRepository cloudCredentialRepository;
  private final CredentialEncryptionService encryptionService;
  private final BillingExportConfigRepository billingExportConfigRepository;
  private final AwsBillingExportConfigRepository awsBillingExportConfigRepository;

  public AwsConnectionPersistenceService(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      CloudCredentialRepository cloudCredentialRepository,
      CredentialEncryptionService encryptionService,
      ResourceRepository resourceRepository,
      BillingExportConfigRepository billingExportConfigRepository,
      ResourceRegistryService resourceRegistryService,
      AwsBillingExportConfigRepository awsBillingExportConfigRepository) {
    super(cloudAccountRepository, resourceRepository, resourceRegistryService);
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.cloudCredentialRepository = cloudCredentialRepository;
    this.encryptionService = encryptionService;
    this.billingExportConfigRepository = billingExportConfigRepository;
    this.awsBillingExportConfigRepository = awsBillingExportConfigRepository;
  }

  @Transactional
  public void persistConnection(PersistAwsConnectionRequest request) {
    CloudConnection connection = getOrCreateConnection(request);
    CloudAccount account = createAccount(connection, request);
    createCredential(account, request.credentials());
    createResources(request.userId(), account, request.resources());
    createBillingExportConfig(account, request.billingConfig());
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
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    CloudAccount account =
        new CloudAccount.Builder()
            .id(UUID.randomUUID())
            .connectionId(connection.getId())
            .accountType(AccountTypeEnum.aws_account)
            .displayName(request.displayName())
            .ingestionPeriod(request.ingestionPeriod().toString())
            .createdAt(now)
            .lastBillingIngestion(now)
            .lastUsageIngestion(now)
            .nextUsageIngestion(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(1))
            .nextBillingIngestion(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(1))
            .build();

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

  private void createBillingExportConfig(CloudAccount account, BillingConfigDto billingConfig) {
    BillingExportConfig config =
        new BillingExportConfig(
            UUID.randomUUID(), account.getId(), OffsetDateTime.now(ZoneOffset.UTC));

    BillingExportConfig savedConfig = billingExportConfigRepository.save(config);

    AwsBillingExportConfig awsConfig =
        new AwsBillingExportConfig(
            savedConfig.getId(),
            billingConfig.bucketName(),
            billingConfig.bucketRegion(),
            billingConfig.exportPrefix(),
            billingConfig.exportName());

    awsBillingExportConfigRepository.save(awsConfig);
  }
}
