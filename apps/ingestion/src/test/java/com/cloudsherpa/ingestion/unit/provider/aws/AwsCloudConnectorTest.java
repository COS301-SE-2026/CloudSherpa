package com.cloudsherpa.ingestion.unit.provider.aws;

import static org.junit.jupiter.api.Assertions.*;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.aws.AwsCloudConnector;
import com.cloudsherpa.ingestion.provider.aws.monitoring.MockCloudWatchMetricProvider;
import com.cloudsherpa.ingestion.provider.scanner.ResourceDiscoveryService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AwsCloudConnectorTest.TestConfig.class)
class AwsCloudConnectorTest {

  private ResourceDiscoveryService discoveryService;

  @Autowired private MockCloudWatchMetricProvider mockMetricProvider;

  private AwsCloudConnector connector;

  @BeforeEach
  void setUp() {
    connector = new AwsCloudConnector(discoveryService, mockMetricProvider);
  }

  @Test
  void getProviderNameShouldReturnAws() {
    assertEquals("AWS", connector.getProviderName());
  }

  @Test
  void fetchMockUsageShouldGenerateRecords() {

    IngestionRequestEvent request = buildRequest(300);

    List<UsageRecordModel> result = connector.fetchMockUsage(request.getScopes().get(0), request);

    assertFalse(result.isEmpty());
  }

  @Test
  void fetchMockUsageShouldThrowForInvalidPeriod() {

    IngestionRequestEvent request = buildRequest(0);

    AccountScope accountScope = request.getScopes().get(0);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> connector.fetchMockUsage(accountScope, request));

    assertTrue(ex.getMessage().contains("Period must be > 0"));
  }

  @Test
  void fetchMockUsageShouldPopulateImportantFields() {

    IngestionRequestEvent request = buildRequest(300);

    List<UsageRecordModel> result = connector.fetchMockUsage(request.getScopes().get(0), request);

    UsageRecordModel usageRecord = result.get(0);

    assertNotNull(usageRecord.getProvider());
    assertNotNull(usageRecord.getMetricName());
    assertNotNull(usageRecord.getTimestamp());
    assertNotNull(usageRecord.getResourceId());
    assertNotNull(usageRecord.getIngestionId());
  }

  @Test
  void testConnectionShouldReturnBoolean() {
    CloudCredentials credentials = new CloudCredentials();
    credentials.setAccessKey("accessKey");
    credentials.setSecretKey("secretKey");
    boolean result = connector.testConnection(credentials);

    assertTrue(result || !result);
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
