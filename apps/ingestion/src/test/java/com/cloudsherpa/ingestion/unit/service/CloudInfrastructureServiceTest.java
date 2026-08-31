package com.cloudsherpa.ingestion.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.service.CloudInfrastructureService;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CloudInfrastructureServiceTest {

  private CloudConnectionRepository connectionRepo;
  private CloudAccountRepository accountRepo;
  private ResourceRepository resourceRepo;
  private CloudInfrastructureService service;

  @BeforeEach
  void setUp() {
    connectionRepo = mock(CloudConnectionRepository.class);
    accountRepo = mock(CloudAccountRepository.class);
    resourceRepo = mock(ResourceRepository.class);

    service = new CloudInfrastructureService(connectionRepo, accountRepo, resourceRepo);
  }

  @Test
  void ensureInfrastructureShouldReturnResourceWhenAllEntitiesExist() {
    UUID userId = UUID.randomUUID();
    UUID connectionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    UsageRecordModel r =
        buildMockUsageRecord("AWS", accountId.toString(), "EC2", "i-123456", "us-east-1");
    CloudConnection mockConnection = buildMockConnection(connectionId);
    CloudAccount mockAccount = buildMockAccount(accountId, connectionId);
    Resource expectedResource = mock(Resource.class);

    when(connectionRepo.findByUserIdAndProvider(userId, ProviderEnum.AWS))
        .thenReturn(List.of(mockConnection));
    when(accountRepo.findById(accountId)).thenReturn(Optional.of(mockAccount));
    when(resourceRepo.findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            accountId, "EC2", "i-123456", "us-east-1"))
        .thenReturn(Optional.of(expectedResource));

    Resource result = service.ensureInfrastructure(r, userId);

    assertNotNull(result);
    assertEquals(expectedResource, result);

    verify(connectionRepo, times(1)).findByUserIdAndProvider(userId, ProviderEnum.AWS);
    verify(accountRepo, times(1)).findById(accountId);
    verify(resourceRepo, times(1))
        .findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            any(), anyString(), anyString(), anyString());
  }

  @Test
  void ensureInfrastructureShouldThrowWhenConnectionNotFound() {
    UUID userId = UUID.randomUUID();
    UsageRecordModel r =
        buildMockUsageRecord("AWS", UUID.randomUUID().toString(), "EC2", "i-123", "us-east-1");

    when(connectionRepo.findByUserIdAndProvider(userId, ProviderEnum.AWS)).thenReturn(List.of());

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.ensureInfrastructure(r, userId));

    assertTrue(exception.getMessage().contains("Cloud connection not found for user"));
    verify(accountRepo, never()).findById(any());
  }

  @Test
  void ensureInfrastructureShouldThrowWhenAccountNotFound() {
    UUID userId = UUID.randomUUID();
    UUID connectionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    UsageRecordModel r =
        buildMockUsageRecord("AWS", accountId.toString(), "EC2", "i-123", "us-east-1");
    CloudConnection mockConnection = buildMockConnection(connectionId);

    when(connectionRepo.findByUserIdAndProvider(userId, ProviderEnum.AWS))
        .thenReturn(List.of(mockConnection));

    when(accountRepo.findById(accountId)).thenReturn(Optional.empty());

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.ensureInfrastructure(r, userId));

    assertTrue(exception.getMessage().contains("Cloud account not found"));
    verify(resourceRepo, never())
        .findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(any(), any(), any(), any());
  }

  @Test
  void ensureInfrastructureShouldThrowWhenAccountConnectionIdMismatches() {
    UUID userId = UUID.randomUUID();
    UUID connectionId = UUID.randomUUID();
    UUID mismatchedConnectionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    UsageRecordModel r =
        buildMockUsageRecord("AWS", accountId.toString(), "EC2", "i-123", "us-east-1");
    CloudConnection mockConnection = buildMockConnection(connectionId);

    CloudAccount mockAccount = buildMockAccount(accountId, mismatchedConnectionId);

    when(connectionRepo.findByUserIdAndProvider(userId, ProviderEnum.AWS))
        .thenReturn(List.of(mockConnection));
    when(accountRepo.findById(accountId)).thenReturn(Optional.of(mockAccount));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> service.ensureInfrastructure(r, userId));

    assertTrue(exception.getMessage().contains("does not belong to connection"));
    verify(resourceRepo, never())
        .findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(any(), any(), any(), any());
  }

  private UsageRecordModel buildMockUsageRecord(
      String provider, String accountKey, String serviceName, String resourceId, String region) {
    UsageRecordModel mockRecord = mock(UsageRecordModel.class);

    when(mockRecord.getProvider()).thenReturn(provider);
    when(mockRecord.resolveKey()).thenReturn(accountKey);
    when(mockRecord.getServiceName()).thenReturn(serviceName);
    when(mockRecord.getResourceId()).thenReturn(resourceId);
    when(mockRecord.getRegion()).thenReturn(region);

    return mockRecord;
  }

  private CloudConnection buildMockConnection(UUID id) {
    CloudConnection connection = mock(CloudConnection.class);

    when(connection.getId()).thenReturn(id);

    return connection;
  }

  private CloudAccount buildMockAccount(UUID accountId, UUID connectionId) {
    CloudAccount account = mock(CloudAccount.class);

    when(account.getId()).thenReturn(accountId);
    when(account.getConnectionId()).thenReturn(connectionId);

    return account;
  }
}
