package com.cloudsherpa.service.unit.persistconnection.aws.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.AccountTypeEnum;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.BillingExportConfigRepository;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.service.ResourceRegistryService;
import com.cloudsherpa.service.persistconnection.dto.ResourceSelectionDto;
import com.cloudsherpa.service.persistconnection.provider.aws.dto.AwsCredentialsDto;
import com.cloudsherpa.service.persistconnection.provider.aws.dto.BillingConfigDto;
import com.cloudsherpa.service.persistconnection.provider.aws.dto.PersistAwsConnectionRequest;
import com.cloudsherpa.service.persistconnection.provider.aws.service.AwsConnectionPersistenceService;
import com.cloudsherpa.service.persistconnection.service.CredentialEncryptionService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AwsConnectionPersistenceServiceTest {

  @Mock private CloudConnectionRepository cloudConnectionRepository;

  @Mock private CloudAccountRepository cloudAccountRepository;

  @Mock private CloudCredentialRepository cloudCredentialRepository;

  @Mock private CredentialEncryptionService encryptionService;

  @Mock private ResourceRepository resourceRepository;

  @Mock private BillingExportConfigRepository billingExportConfigRepository;

  @Mock private ResourceRegistryService resourceRegistryService;

  @InjectMocks private AwsConnectionPersistenceService service;

  @Captor private ArgumentCaptor<CloudConnection> connectionCaptor;

  @Captor private ArgumentCaptor<CloudAccount> accountCaptor;

  @Captor private ArgumentCaptor<CloudCredential> credentialCaptor;

  @Captor private ArgumentCaptor<List<Resource>> resourceCaptor;

  @Captor private ArgumentCaptor<String> encryptionJsonCaptor;

  private PersistAwsConnectionRequest request;
  private CloudConnection existingConnection;
  private CloudAccount savedAccount;

  @BeforeEach
  void setUp() {

    AwsCredentialsDto credentials = new AwsCredentialsDto("accessKey", "secretKey");
    ResourceSelectionDto activeResource =
        new ResourceSelectionDto(
            "i-12345",
            "EC2",
            "instanceId",
            "instance-1",
            "af-south-1",
            Map.of("Environment", "Prod"),
            true);

    ResourceSelectionDto disabledResource =
        new ResourceSelectionDto(
            "i-23456", "S3", "BucketName", "bucket-1", "af-south-1", Map.of(), false);
    BillingConfigDto billingConfig =
        new BillingConfigDto("billing-bucket", "eu-north-1", "exports/", "daily-cost-export");

    request =
        new PersistAwsConnectionRequest(
            UUID.randomUUID(),
            null,
            "Production",
            300,
            credentials,
            List.of(activeResource, disabledResource),
            billingConfig);

    existingConnection =
        new CloudConnection(
            UUID.randomUUID(),
            request.userId(),
            ProviderEnum.AWS,
            StatusEnum.active,
            OffsetDateTime.now());

    savedAccount =
        new CloudAccount.Builder()
            .id(UUID.randomUUID())
            .connectionId(existingConnection.getId())
            .accountType(AccountTypeEnum.aws_account)
            .displayName("Production")
            .ingestionPeriod("300")
            .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
            .nextUsageIngestion(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(300))
            .nextUsageIngestion(OffsetDateTime.now(ZoneOffset.UTC).plusHours(12))
            .build();
  }

  private void mockExistingConnection() {

    when(cloudConnectionRepository.findByUserIdAndProvider(request.userId(), ProviderEnum.AWS))
        .thenReturn(List.of(existingConnection));
  }

  private void mockNewConnection() {

    when(cloudConnectionRepository.findByUserIdAndProvider(request.userId(), ProviderEnum.AWS))
        .thenReturn(List.of());

    when(cloudConnectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  private void mockAccountSave() {
    when(cloudAccountRepository.save(any())).thenReturn(savedAccount);
  }

  private void mockAccountEchoSave() {
    when(cloudAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  private void mockEncryption() {
    when(encryptionService.encrypt(any())).thenReturn("encrypted-json");
  }

  @Test
  void shouldCreateNewConnectionWhenNoneExists() {

    mockNewConnection();
    mockAccountSave();
    mockEncryption();

    service.persistConnection(request);

    verify(cloudConnectionRepository).save(connectionCaptor.capture());

    CloudConnection saved = connectionCaptor.getValue();

    assertEquals(request.userId(), saved.getUserId());
    assertEquals(ProviderEnum.AWS, saved.getProvider());
    assertEquals(StatusEnum.active, saved.getStatus());

    verify(cloudConnectionRepository).findByUserIdAndProvider(request.userId(), ProviderEnum.AWS);
  }

  @Test
  void shouldReuseExistingConnection() {

    mockExistingConnection();
    mockAccountSave();
    mockEncryption();

    service.persistConnection(request);

    verify(cloudConnectionRepository).findByUserIdAndProvider(request.userId(), ProviderEnum.AWS);

    verify(cloudConnectionRepository, never()).save(any());
  }

  @Test
  void shouldCreateCloudAccount() {

    mockExistingConnection();
    mockAccountEchoSave();
    mockEncryption();

    service.persistConnection(request);

    verify(cloudAccountRepository).save(accountCaptor.capture());

    CloudAccount account = accountCaptor.getValue();

    assertEquals(existingConnection.getId(), account.getConnectionId());
    assertEquals(AccountTypeEnum.aws_account, account.getAccountType());
    assertEquals("Production", account.getDisplayName());
    assertEquals("300", account.getIngestionPeriod());
  }

  @Test
  void shouldEncryptAndSaveCredential() {

    mockExistingConnection();
    mockAccountSave();
    mockEncryption();

    service.persistConnection(request);

    verify(encryptionService).encrypt(encryptionJsonCaptor.capture());

    String json = encryptionJsonCaptor.getValue();

    assertTrue(json.contains("accessKey"));
    assertTrue(json.contains("secretKey"));

    verify(cloudCredentialRepository).save(credentialCaptor.capture());

    CloudCredential credential = credentialCaptor.getValue();

    assertEquals(savedAccount.getId(), credential.getAccountId());
    assertEquals("AWS", credential.getProvider());
    assertEquals("IAM_USER", credential.getCredentialType());
  }

  @Test
  void shouldCreateResources() {

    mockExistingConnection();
    mockAccountSave();
    mockEncryption();

    service.persistConnection(request);

    verify(resourceRepository).saveAll(resourceCaptor.capture());

    List<Resource> resources = resourceCaptor.getValue();

    assertEquals(2, resources.size());

    Resource first = resources.get(0);

    assertEquals(savedAccount.getId(), first.getAccountId());
    assertEquals("EC2", first.getResourceType());
    assertEquals("instance-1", first.getResourceName());
    assertEquals(StatusEnum.active, first.getStatus());
    assertEquals(Map.of("Environment", "Prod"), first.getTags());

    Resource second = resources.get(1);

    assertEquals(savedAccount.getId(), second.getAccountId());
    assertEquals("S3", second.getResourceType());
    assertEquals("bucket-1", second.getResourceName());
    assertEquals(StatusEnum.disabled, second.getStatus());
  }

  @Test
  void shouldHandleEmptyResourceList() {

    PersistAwsConnectionRequest emptyRequest =
        new PersistAwsConnectionRequest(
            request.userId(),
            request.accountId(),
            request.displayName(),
            request.ingestionPeriod(),
            request.credentials(),
            List.of(),
            request.billingConfig());

    when(cloudConnectionRepository.findByUserIdAndProvider(request.userId(), ProviderEnum.AWS))
        .thenReturn(List.of(existingConnection));

    mockAccountSave();
    mockEncryption();

    service.persistConnection(emptyRequest);

    verify(resourceRepository).saveAll(resourceCaptor.capture());

    assertTrue(resourceCaptor.getValue().isEmpty());
  }

  @Test
  void shouldHandleNullTags() {

    ResourceSelectionDto resource =
        new ResourceSelectionDto(
            "i-12345", "EC2", "instanceId", "instance-1", "af-south-1", null, true);

    PersistAwsConnectionRequest requestWithNullTags =
        new PersistAwsConnectionRequest(
            request.userId(),
            request.accountId(),
            request.displayName(),
            request.ingestionPeriod(),
            request.credentials(),
            List.of(resource),
            request.billingConfig());

    when(cloudConnectionRepository.findByUserIdAndProvider(request.userId(), ProviderEnum.AWS))
        .thenReturn(List.of(existingConnection));

    mockAccountSave();
    mockEncryption();

    service.persistConnection(requestWithNullTags);

    verify(resourceRepository).saveAll(resourceCaptor.capture());

    List<Resource> resources = resourceCaptor.getValue();

    assertEquals(1, resources.size());
    assertEquals("EC2", resources.get(0).getResourceType());
    assertEquals("instance-1", resources.get(0).getResourceName());
    assertEquals(StatusEnum.active, resources.get(0).getStatus());
    assertEquals(null, resources.get(0).getTags());
  }

  @Test
  void shouldUseFirstConnectionWhenMultipleExist() {

    CloudConnection secondConnection =
        new CloudConnection(
            UUID.randomUUID(),
            request.userId(),
            ProviderEnum.AWS,
            StatusEnum.active,
            OffsetDateTime.now());

    when(cloudConnectionRepository.findByUserIdAndProvider(request.userId(), ProviderEnum.AWS))
        .thenReturn(List.of(existingConnection, secondConnection));

    mockAccountEchoSave();
    mockEncryption();

    service.persistConnection(request);

    verify(cloudAccountRepository).save(accountCaptor.capture());

    CloudAccount account = accountCaptor.getValue();

    assertEquals(existingConnection.getId(), account.getConnectionId());

    verify(cloudConnectionRepository, never()).save(any());
  }

  @Test
  void shouldPropagateAccountRepositoryException() {

    mockExistingConnection();

    when(cloudAccountRepository.save(any())).thenThrow(new RuntimeException("Database failure"));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> service.persistConnection(request));

    assertEquals("Database failure", exception.getMessage());

    verify(cloudCredentialRepository, never()).save(any());

    verify(resourceRepository, never()).saveAll(any());
  }

  @Test
  void shouldPropagateResourceRepositoryException() {

    mockExistingConnection();
    mockAccountSave();
    mockEncryption();

    when(resourceRepository.saveAll(any()))
        .thenThrow(new RuntimeException("Unable to save resources"));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> service.persistConnection(request));

    assertEquals("Unable to save resources", exception.getMessage());
  }

  @Test
  void shouldThrowWhenEncryptionFails() {

    mockExistingConnection();
    mockAccountSave();

    when(encryptionService.encrypt(any())).thenThrow(new RuntimeException("Encryption failed"));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> service.persistConnection(request));

    assertEquals("Encryption failed", exception.getMessage());

    verify(cloudCredentialRepository, never()).save(any());

    verify(resourceRepository, never()).saveAll(any());
  }

  @Test
  void shouldPersistEntitiesInCorrectOrder() {

    mockExistingConnection();
    mockAccountSave();
    mockEncryption();

    service.persistConnection(request);

    InOrder inOrder =
        inOrder(cloudAccountRepository, cloudCredentialRepository, resourceRepository);

    inOrder.verify(cloudAccountRepository).save(any(CloudAccount.class));
    inOrder.verify(cloudCredentialRepository).save(any(CloudCredential.class));
    inOrder.verify(resourceRepository).saveAll(any());

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldCreateConnectionBeforePersistingOtherEntities() {

    mockNewConnection();
    mockAccountSave();
    mockEncryption();

    service.persistConnection(request);

    InOrder inOrder =
        inOrder(
            cloudConnectionRepository,
            cloudAccountRepository,
            cloudCredentialRepository,
            resourceRepository);

    inOrder
        .verify(cloudConnectionRepository)
        .findByUserIdAndProvider(request.userId(), ProviderEnum.AWS);

    inOrder.verify(cloudConnectionRepository).save(any(CloudConnection.class));

    inOrder.verify(cloudAccountRepository).save(any(CloudAccount.class));

    inOrder.verify(cloudCredentialRepository).save(any(CloudCredential.class));

    inOrder.verify(resourceRepository).saveAll(any());

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldReuseConnectionAndPersistRemainingEntitiesInOrder() {

    mockExistingConnection();
    mockAccountSave();
    mockEncryption();

    service.persistConnection(request);

    InOrder inOrder =
        inOrder(
            cloudConnectionRepository,
            cloudAccountRepository,
            cloudCredentialRepository,
            resourceRepository);

    inOrder
        .verify(cloudConnectionRepository)
        .findByUserIdAndProvider(request.userId(), ProviderEnum.AWS);

    inOrder.verify(cloudAccountRepository).save(any(CloudAccount.class));

    inOrder.verify(cloudCredentialRepository).save(any(CloudCredential.class));

    inOrder.verify(resourceRepository).saveAll(any());

    verify(cloudConnectionRepository, never()).save(any());

    inOrder.verifyNoMoreInteractions();
  }
}
