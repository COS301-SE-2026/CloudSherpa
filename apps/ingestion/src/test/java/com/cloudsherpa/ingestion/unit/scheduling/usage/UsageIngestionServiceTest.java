package com.cloudsherpa.ingestion.unit.scheduling.usage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.scheduler.encryption.CredentialEncryptionService;
import com.cloudsherpa.ingestion.scheduler.usage.UsageIngestionClient;
import com.cloudsherpa.ingestion.scheduler.usage.UsageIngestionService;
import com.cloudsherpa.ingestion.service.TenantSchemaService;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.CloudCredential;
import com.cloudsherpa.lib.entities.OfferedMetric;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.entities.StatusEnum;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudCredentialRepository;
import com.cloudsherpa.lib.repositories.OfferedMetricRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsageIngestionServiceTest {

  @Mock private UsageIngestionClient client;

  @Mock private CloudAccountRepository cloudAccountRepository;

  @Mock private CloudCredentialRepository cloudCredentialRepository;

  @Mock private ResourceRepository resourceRepository;

  @Mock private OfferedMetricRepository offeredMetricRepository;

  @Mock private CredentialEncryptionService encryptionService;

  @Mock private TenantSchemaService tenantSchemaService;

  @Mock private ObjectMapper mapper;

  @InjectMocks private UsageIngestionService service;

  private UUID accountId;
  private UUID userId;
  private CloudAccount account;
  private CloudCredential credential;

  @BeforeEach
  void setUp() {
    accountId = UUID.randomUUID();
    userId = UUID.randomUUID();

    account = mock(CloudAccount.class);
    credential = mock(CloudCredential.class);
  }

  private void setupSuccessfulIngestion() throws Exception {
    CloudConnection connection = mockConnection();

    when(cloudAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

    when(cloudCredentialRepository.findByAccountId(accountId)).thenReturn(List.of(credential));

    when(credential.getCredentialValue()).thenReturn("encrypted-credentials");

    when(encryptionService.decrypt("encrypted-credentials"))
        .thenReturn(
            "{\"accessKey\":\"key\",\"secretKey\":\"secret\",\"awsRegion\":\"eu-north-1\"}");

    when(account.getConnection()).thenReturn(connection);

    when(account.getLastUsageIngestion())
        .thenReturn(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5));

    when(account.getIngestionPeriod()).thenReturn("300");

    when(resourceRepository.findByAccountId(accountId)).thenReturn(List.of());

    CloudCredentials cloudCredentials = mock(CloudCredentials.class);

    when(mapper.readValue(anyString(), eq(CloudCredentials.class))).thenReturn(cloudCredentials);
  }

  private com.cloudsherpa.lib.entities.CloudConnection mockConnection() {
    com.cloudsherpa.lib.entities.CloudConnection connection =
        mock(com.cloudsherpa.lib.entities.CloudConnection.class);

    var user = mock(com.cloudsherpa.lib.entities.User.class);

    when(user.getId()).thenReturn(userId);

    when(connection.getUser()).thenReturn(user);
    when(connection.getUserId()).thenReturn(userId);
    when(connection.getProvider()).thenReturn(ProviderEnum.AWS);

    return connection;
  }

  @Test
  void ingest_shouldThrowWhenAccountDoesNotExist() {
    when(cloudAccountRepository.findById(accountId)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.ingest(accountId));

    assertTrue(exception.getMessage().contains("Cloud account not found"));

    verifyNoInteractions(client);
  }

  @Test
  void ingest_shouldDecryptCredentialsAndSendRequest() throws Exception {
    setupSuccessfulIngestion();
    service.ingest(accountId);

    verify(encryptionService).decrypt("encrypted-credentials");

    verify(mapper).readValue(anyString(), eq(CloudCredentials.class));

    verify(client).ingest(any(IngestionRequestEvent.class));
  }

  @Test
  void ingest_shouldBuildRequestWithCorrectAccountScope() throws Exception {
    setupSuccessfulIngestion();
    service.ingest(accountId);

    ArgumentCaptor<IngestionRequestEvent> captor =
        ArgumentCaptor.forClass(IngestionRequestEvent.class);

    verify(client).ingest(captor.capture());

    IngestionRequestEvent request = captor.getValue();

    assertNotNull(request.getScopes());
    assertEquals(1, request.getScopes().size());

    assertEquals(accountId.toString(), request.getScopes().getFirst().getAccountId());
    assertEquals(ProviderEnum.AWS.toString(), request.getScopes().getFirst().getProvider());
  }

  @Test
  void ingest_shouldSetUsageConfiguration() throws Exception {
    setupSuccessfulIngestion();
    service.ingest(accountId);

    ArgumentCaptor<IngestionRequestEvent> captor =
        ArgumentCaptor.forClass(IngestionRequestEvent.class);

    verify(client).ingest(captor.capture());

    IngestionRequestEvent request = captor.getValue();

    assertTrue(request.isIncludeUsage());
    assertEquals(60, request.getPeriod());
    assertEquals(userId, request.getUserId());
    assertNotNull(request.getFrom());
    assertNotNull(request.getTo());
  }

  @Test
  void ingest_shouldCreateServiceScopeForEachDistinctResourceType() throws Exception {
    setupSuccessfulIngestion();
    Resource ec2Resource1 = resource("EC2", "InstanceId", "i-123", "eu-north-1");
    Resource ec2Resource2 = resource("EC2", "InstanceId", "i-456", "eu-north-1");
    Resource ecsResource = resource("ECS", "ClusterName", "cluster-1", "eu-north-1");

    when(resourceRepository.findByAccountId(accountId))
        .thenReturn(List.of(ec2Resource1, ec2Resource2, ecsResource));

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierType(
            accountId, "EC2", "InstanceId"))
        .thenReturn(List.of(ec2Resource1, ec2Resource2));

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierType(
            accountId, "ECS", "ClusterName"))
        .thenReturn(List.of(ecsResource));

    when(offeredMetricRepository.findByProviderAndServiceType(ProviderEnum.AWS, "EC2"))
        .thenReturn(List.of());

    when(offeredMetricRepository.findByProviderAndServiceType(ProviderEnum.AWS, "ECS"))
        .thenReturn(List.of());

    service.ingest(accountId);

    ArgumentCaptor<IngestionRequestEvent> captor =
        ArgumentCaptor.forClass(IngestionRequestEvent.class);

    verify(client).ingest(captor.capture());

    var scopes = captor.getValue().getScopes().getFirst().getServiceScopes();

    assertEquals(2, scopes.size());

    assertTrue(scopes.stream().anyMatch(scope -> scope.getName().equals("EC2")));

    assertTrue(scopes.stream().anyMatch(scope -> scope.getName().equals("ECS")));
  }

  @Test
  void ingest_shouldIncludeOfferedMetricsInServiceScope() throws Exception {
    setupSuccessfulIngestion();
    Resource resource = resource("EC2", "InstanceId", "i-123", "eu-north-1");

    OfferedMetric cpuMetric = mock(OfferedMetric.class);
    OfferedMetric networkMetric = mock(OfferedMetric.class);

    when(cpuMetric.getMetricName()).thenReturn("CPUUtilization");
    when(cpuMetric.getExpectedUnit()).thenReturn("Percent");

    when(networkMetric.getMetricName()).thenReturn("NetworkIn");
    when(networkMetric.getExpectedUnit()).thenReturn("Bytes");

    when(resourceRepository.findByAccountId(accountId)).thenReturn(List.of(resource));

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierType(
            accountId, "EC2", "InstanceId"))
        .thenReturn(List.of(resource));

    when(offeredMetricRepository.findByProviderAndServiceType(ProviderEnum.AWS, "EC2"))
        .thenReturn(List.of(cpuMetric, networkMetric));

    service.ingest(accountId);

    ArgumentCaptor<IngestionRequestEvent> captor =
        ArgumentCaptor.forClass(IngestionRequestEvent.class);

    verify(client).ingest(captor.capture());

    var serviceScope = captor.getValue().getScopes().getFirst().getServiceScopes().getFirst();

    assertEquals(2, serviceScope.getMetrics().size());

    assertTrue(
        serviceScope.getMetrics().stream()
            .anyMatch(
                metric ->
                    metric.getName().equals("CPUUtilization")
                        && metric.getUnit().equals("Percent")));

    assertTrue(
        serviceScope.getMetrics().stream()
            .anyMatch(
                metric ->
                    metric.getName().equals("NetworkIn") && metric.getUnit().equals("Bytes")));
  }

  @Test
  void ingest_shouldIncludeEnabledResources() throws Exception {
    setupSuccessfulIngestion();
    Resource resource = resource("EC2", "InstanceId", "i-123", "eu-north-1");

    when(resource.getStatus()).thenReturn(StatusEnum.active);

    when(resourceRepository.findByAccountId(accountId)).thenReturn(List.of(resource));

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierType(
            accountId, "EC2", "InstanceId"))
        .thenReturn(List.of(resource));

    service.ingest(accountId);

    ArgumentCaptor<IngestionRequestEvent> captor =
        ArgumentCaptor.forClass(IngestionRequestEvent.class);

    verify(client).ingest(captor.capture());

    var instances =
        captor
            .getValue()
            .getScopes()
            .getFirst()
            .getServiceScopes()
            .getFirst()
            .getInstances()
            .getFirst()
            .getInstances();

    assertEquals(1, instances.size());
    assertEquals("i-123", instances.getFirst().getIdentifier());
    assertEquals("eu-north-1", instances.getFirst().getRegion());
  }

  @Test
  void ingest_shouldIgnoreDisabledResources() throws Exception {
    setupSuccessfulIngestion();

    Resource enabled = mock(Resource.class);
    Resource disabled = mock(Resource.class);

    // Enabled resource
    when(enabled.getResourceType()).thenReturn("EC2");
    when(enabled.getResourceIdentifierType()).thenReturn("InstanceId");
    when(enabled.getResourceIdentifier()).thenReturn("i-enabled");
    when(enabled.getRegion()).thenReturn("eu-north-1");
    when(enabled.getStatus()).thenReturn(StatusEnum.active);

    // Disabled resource
    when(disabled.getResourceType()).thenReturn("EC2");
    when(disabled.getStatus()).thenReturn(StatusEnum.disabled);

    when(resourceRepository.findByAccountId(accountId)).thenReturn(List.of(enabled, disabled));

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierType(
            accountId, "EC2", "InstanceId"))
        .thenReturn(List.of(enabled, disabled));

    service.ingest(accountId);

    ArgumentCaptor<IngestionRequestEvent> captor =
        ArgumentCaptor.forClass(IngestionRequestEvent.class);

    verify(client).ingest(captor.capture());

    var serviceScopes = captor.getValue().getScopes().getFirst().getServiceScopes();

    assertEquals(1, serviceScopes.size());

    var instances = serviceScopes.getFirst().getInstances().getFirst().getInstances();

    assertEquals(1, instances.size());
    assertEquals("i-enabled", instances.getFirst().getIdentifier());

    assertFalse(
        instances.stream().anyMatch(instance -> "i-disabled".equals(instance.getIdentifier())));
  }

  @Test
  void ingest_shouldUpdateLastAndNextUsageIngestionAfterSuccessfulIngestion() throws Exception {
    setupSuccessfulIngestion();
    service.ingest(accountId);

    verify(cloudAccountRepository).save(account);

    verify(account).setLastUsageIngestion(any());
    verify(account).setNextUsageIngestion(any());
  }

  private Resource resource(String type, String identifierType, String identifier, String region) {

    Resource resource = mock(Resource.class);

    when(resource.getResourceType()).thenReturn(type);
    when(resource.getResourceIdentifierType()).thenReturn(identifierType);
    when(resource.getResourceIdentifier()).thenReturn(identifier);
    when(resource.getRegion()).thenReturn(region);
    when(resource.getStatus()).thenReturn(StatusEnum.active);

    return resource;
  }
}
