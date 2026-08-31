package com.cloudsherpa.ingestion.unit.service;

import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.service.CloudInfrastructureService;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;

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
