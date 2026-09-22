package com.cloudsherpa.service.persistconnection.provider.azure.service;

import com.cloudsherpa.lib.entities.AccountTypeEnum;
import com.cloudsherpa.lib.entities.AzureBillingExportConfig;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.AzureBillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.service.ResourceRegistryService;
import com.cloudsherpa.service.persistconnection.provider.azure.dto.AzureBillingConfigDto;
import com.cloudsherpa.service.persistconnection.provider.azure.dto.AzureCredentialsDto;
import com.cloudsherpa.service.persistconnection.provider.azure.dto.PersistAzureConnectionRequest;
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
public class AzureConnectionPersistenceService extends ConnectionPersistenceService {
  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;
  private final CloudCredentialRepository cloudCredentialRepository;
  private final CredentialEncryptionService encryptionService;
  private final BillingExportConfigRepository billingExportConfigRepository;
  private final AzureBillingExportConfigRepository azureBillingExportConfigRepository;

  public AzureConnectionPersistenceService(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      CloudCredentialRepository cloudCredentialRepository,
      CredentialEncryptionService encryptionService,
      ResourceRepository resourceRepository,
      ResourceRegistryService resourceRegistryService,
      BillingExportConfigRepository billingExportConfigRepository,
      AzureBillingExportConfigRepository azureBillingExportConfigRepository) {
    super(cloudAccountRepository, resourceRepository, resourceRegistryService);
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.cloudCredentialRepository = cloudCredentialRepository;
    this.encryptionService = encryptionService;
    this.billingExportConfigRepository = billingExportConfigRepository;
    this.azureBillingExportConfigRepository = azureBillingExportConfigRepository;
  }

  @Transactional
  public void persistConnection(PersistAzureConnectionRequest request) {
    CloudConnection connection = getOrCreateConnection(request);
    CloudAccount account = createAccount(connection, request);
    createCredential(account, request.credentials());
    createResources(request.userId(), account, request.resources());
    if (request.billingConfig() != null) {
      createBillingExportConfig(account, request.billingConfig());
    }
  }

  private CloudConnection getOrCreateConnection(PersistAzureConnectionRequest request) {

    List<CloudConnection> optionalConnection =
        cloudConnectionRepository.findByUserIdAndProvider(request.userId(), ProviderEnum.AZURE);

    if (optionalConnection.isEmpty()) {
      UUID connectionId = UUID.randomUUID();
      CloudConnection connection =
          new CloudConnection(
              connectionId,
              request.userId(),
              ProviderEnum.AZURE,
              StatusEnum.active,
              OffsetDateTime.now(ZoneOffset.UTC));
      return cloudConnectionRepository.save(connection);
    }
    return optionalConnection.getFirst();
  }

  private CloudAccount createAccount(
      CloudConnection connection, PersistAzureConnectionRequest request) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    CloudAccount account =
        new CloudAccount.Builder()
            .id(UUID.randomUUID())
            .connectionId(connection.getId())
            .accountType(AccountTypeEnum.azure_subscription)
            .displayName(request.displayName())
            .ingestionPeriod(request.ingestionPeriod().toString())
            .createdAt(now)
            .lastBillingIngestion(now)
            .lastUsageIngestion(now.minusDays(1))
            .nextUsageIngestion(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(1))
            .nextBillingIngestion(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(1))
            .build();

    return cloudAccountRepository.save(account);
  }

  private void createCredential(CloudAccount account, AzureCredentialsDto credentials) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String saveJson = objectMapper.writeValueAsString(credentials);

      String encrypted = encryptionService.encrypt(saveJson);

      CloudCredential credential =
          new CloudCredential(
              UUID.randomUUID(),
              account.getId(),
              "AZURE",
              "SERVICE_PRINCIPAL",
              encrypted,
              OffsetDateTime.now(ZoneOffset.UTC));
      cloudCredentialRepository.save(credential);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize Azure credentials.", e);
    }
  }

  private void createBillingExportConfig(
      CloudAccount account, AzureBillingConfigDto billingConfigDto) {
    BillingExportConfig config =
        new BillingExportConfig(
            UUID.randomUUID(), account.getId(), OffsetDateTime.now(ZoneOffset.UTC));

    BillingExportConfig savedConfig = billingExportConfigRepository.save(config);

    AzureBillingExportConfig azureExportConfig =
        new AzureBillingExportConfig(
            savedConfig.getId(),
            billingConfigDto.storageAccountName(),
            billingConfigDto.blobContainerName(),
            billingConfigDto.exportDirectory(),
            billingConfigDto.exportName());
    azureBillingExportConfigRepository.save(azureExportConfig);
  }
}
