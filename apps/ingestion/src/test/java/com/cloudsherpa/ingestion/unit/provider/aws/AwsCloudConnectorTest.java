package com.cloudsherpa.ingestion.unit.provider.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.normalization.normalizers.Normalizer;
import com.cloudsherpa.ingestion.provider.aws.AwsCloudConnector;
import com.cloudsherpa.ingestion.provider.aws.monitoring.MockCloudWatchMetricProvider;
import com.cloudsherpa.ingestion.provider.scanner.ResourceDiscoveryService;
import com.cloudsherpa.ingestion.service.IngestionPersistenceService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

class AwsCloudConnectorTest {

  private ResourceDiscoveryService discoveryService;
  private MockCloudWatchMetricProvider mockMetricProvider;
  private IngestionPersistenceService ingestionPersistenceService;

  private AwsCloudConnector connector;

  private Normalizer normalizer;

  @BeforeEach
  void setUp() {
    discoveryService = mock(ResourceDiscoveryService.class);
    mockMetricProvider = mock(MockCloudWatchMetricProvider.class);
    ingestionPersistenceService = mock(IngestionPersistenceService.class);
    normalizer = mock(Normalizer.class);

    connector =
        new AwsCloudConnector(discoveryService, mockMetricProvider, ingestionPersistenceService);
  }

  @Test
  void getProviderNameShouldReturnAws() {
    assertEquals("AWS", connector.getProviderName());
  }

  @Test
  void fetchMockUsageShouldDelegateToMetricProvider() {

    IngestionRequestEvent request = buildRequest(300);
    AccountScope accountScope = request.getScopes().get(0);

    connector.fetchMockUsage(accountScope, request, normalizer);

    verify(mockMetricProvider).collectMetrics(accountScope, request, normalizer);
  }

  private IngestionRequestEvent buildRequest(int period) {

    IngestionRequestEvent request = new IngestionRequestEvent();

    request.setFrom(Instant.now().minusSeconds(3600));
    request.setTo(Instant.now());
    request.setPeriod(period);
    InstanceScope instanceScope = new InstanceScope();
    instanceScope.setIdentifierName("InstanceId");
    com.cloudsherpa.ingestion.connector.Instance instance =
        new com.cloudsherpa.ingestion.connector.Instance();
    instance.setIdentifier("i-123");
    instance.setRegion("af-south-1");
    instanceScope.setInstances(List.of(instance));

    Metric metric = new Metric();
    metric.setName("CPUUtilization");
    ServiceScope service = new ServiceScope();
    service.setName("AWS/EC2");
    service.setMetrics(List.of(metric));
    service.setInstances(List.of(instanceScope));

    AccountScope scope = new AccountScope();
    scope.setProvider("AWS");
    scope.setAccountId("123");
    scope.setServiceScopes(List.of(service));

    request.setScopes(List.of(scope));

    return request;
  }

  @Configuration
  @ComponentScan(
      basePackages = {
        "com.cloudsherpa.ingestion.provider.aws.monitoring",
        "com.cloudsherpa.ingestion.provider.mock"
      })
  static class TestConfig {}
}
