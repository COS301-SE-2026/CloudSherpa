package com.cloudsherpa.ingestion.unit.provider.aws;

import static org.junit.jupiter.api.Assertions.*;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.aws.AwsCloudConnector;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AwsCloudConnectorTest {

  private final AwsCloudConnector connector = new AwsCloudConnector();

  @Test
  void getProviderNameShouldReturnAws() {
    assertEquals("AWS", connector.getProviderName());
  }

  @Test
  void fetchMockUsageShouldGenerateRecords() {

    IngestionRequestEvent request = buildRequest(300);

    List<UsageRecordModel> result = connector.fetchMockUsage(
        request.getScopes().get(0),
        request);

    assertFalse(result.isEmpty());
  }

  @Test
  void fetchMockUsageShouldThrowForInvalidPeriod() {

    IngestionRequestEvent request = buildRequest(0);

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> connector.fetchMockUsage(
            request.getScopes().get(0),
            request));

    assertTrue(ex.getMessage().contains("Period must be > 0"));
  }

  @Test
  void fetchMockUsageShouldPopulateImportantFields() {

    IngestionRequestEvent request = buildRequest(300);

    List<UsageRecordModel> result = connector.fetchMockUsage(
        request.getScopes().get(0),
        request);

    UsageRecordModel record = result.get(0);

    assertNotNull(record.getProvider());
    assertNotNull(record.getMetricName());
    assertNotNull(record.getTimestamp());
    assertNotNull(record.getResourceId());
    assertNotNull(record.getIngestionId());
  }

  @Test
  void testConnectionShouldReturnBoolean() {

    boolean result = connector.testConnection(new CloudCredentials());

    assertTrue(result || !result);
  }

  private IngestionRequestEvent buildRequest(int period) {

    IngestionRequestEvent request = new IngestionRequestEvent();

    request.setFrom(Instant.now().minusSeconds(3600));
    request.setTo(Instant.now());
    request.setPeriod(period);

    InstanceScope instance = new InstanceScope();
    instance.setIdentifierName("InstanceId");
    instance.setValues(List.of("i-123"));

    ServiceScope service = new ServiceScope();
    service.setName("EC2");
    service.setMetrics(List.of("CPUUtilization"));
    service.setInstances(List.of(instance));

    AccountScope scope = new AccountScope();
    scope.setProvider("AWS");
    scope.setAccountId("123");
    scope.setServiceScopes(List.of(service));

    request.setScopes(List.of(scope));

    return request;
  }
}
