package com.cloudsherpa.service.persistconnection.provider.gcp.service;

import com.cloudsherpa.lib.entities.AccountTypeEnum;
import com.cloudsherpa.lib.entities.BillingExportConfig;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.entities.GcpBillingExportConfig;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.cloudsherpa.lib.repositories.GcpBillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.service.ResourceRegistryService;
import com.cloudsherpa.service.persistconnection.provider.gcp.dto.GcpBillingConfigDto;
import com.cloudsherpa.service.persistconnection.provider.gcp.dto.GcpCredentialsDto;
import com.cloudsherpa.service.persistconnection.provider.gcp.dto.PersistGcpConnectionRequest;
import com.cloudsherpa.service.persistconnection.service.ConnectionPersistenceService;
import com.cloudsherpa.service.persistconnection.service.CredentialEncryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GcpConnectionPersistenceService extends ConnectionPersistenceService {
  private static final Logger logger =
      LoggerFactory.getLogger(GcpConnectionPersistenceService.class);

  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;
  private final CloudCredentialRepository cloudCredentialRepository;
  private final CredentialEncryptionService encryptionService;
  private final BillingExportConfigRepository billingExportConfigRepository;
  private final GcpBillingExportConfigRepository gcpBillingExportConfigRepository;

  public GcpConnectionPersistenceService(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      CloudCredentialRepository cloudCredentialRepository,
      CredentialEncryptionService encryptionService,
      ResourceRepository resourceRepository,
      ResourceRegistryService resourceRegistryService,
      BillingExportConfigRepository billingExportConfigRepository,
      GcpBillingExportConfigRepository gcpBillingExportConfigRepository) {
    super(cloudAccountRepository, resourceRepository, resourceRegistryService);
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.cloudCredentialRepository = cloudCredentialRepository;
    this.encryptionService = encryptionService;
    this.billingExportConfigRepository = billingExportConfigRepository;
    this.gcpBillingExportConfigRepository = gcpBillingExportConfigRepository;
  }

  @Transactional
  public void persistConnection(PersistGcpConnectionRequest request) {
    CloudConnection connection = getOrCreateConnection(request);
    CloudAccount account = createAccount(connection, request);
    createCredential(account, request.credentials());
    createResources(request.userId(), account, request.resources());
    if (request.billingConfig() != null) {
      logger.info("Persisting GCP billing config for user {}", request.userId());
      createBillingExportConfig(account, request.billingConfig());
    }
  }

  private CloudConnection getOrCreateConnection(PersistGcpConnectionRequest request) {

    List<CloudConnection> optionalConnection =
        cloudConnectionRepository.findByUserIdAndProvider(request.userId(), ProviderEnum.GCP);

    if (optionalConnection.isEmpty()) {
      UUID connectionId = UUID.randomUUID();
      CloudConnection connection =
          new CloudConnection(
              connectionId,
              request.userId(),
              ProviderEnum.GCP,
              StatusEnum.active,
              OffsetDateTime.now(ZoneOffset.UTC));
      return cloudConnectionRepository.save(connection);
    }
    return optionalConnection.getFirst();
  }

  private CloudAccount createAccount(
      CloudConnection connection, PersistGcpConnectionRequest request) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    CloudAccount account =
        new CloudAccount.Builder()
            .id(UUID.randomUUID())
            .connectionId(connection.getId())
            .accountType(AccountTypeEnum.gcp_project)
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

  private void createCredential(CloudAccount account, GcpCredentialsDto credentials) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String saveJson = objectMapper.writeValueAsString(credentials);

      String encrypted = encryptionService.encrypt(saveJson);

      CloudCredential credential =
          new CloudCredential(
              UUID.randomUUID(),
              account.getId(),
              "GCP",
              "SERVICE_ACCOUNT",
              encrypted,
              OffsetDateTime.now(ZoneOffset.UTC));
      cloudCredentialRepository.save(credential);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to serialize GCP credentials.", e);
    }
  }

  private void createBillingExportConfig(
      CloudAccount account, GcpBillingConfigDto billingConfigDto) {
    BillingExportConfig config =
        new BillingExportConfig(
            UUID.randomUUID(), account.getId(), OffsetDateTime.now(ZoneOffset.UTC));

    BillingExportConfig savedConfig = billingExportConfigRepository.save(config);

    GcpBillingExportConfig gcpExportConfig =
        new GcpBillingExportConfig(
            savedConfig.getId(), billingConfigDto.dataset(), billingConfigDto.billingId());
    gcpBillingExportConfigRepository.save(gcpExportConfig);
  }
}
