package com.cloudsherpa.ingestion.unit.normalization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.normalization.normalizers.AzureNormalizer;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AzureNormalizerTest {

  private final ResourceRepository resourceRepository = mock(ResourceRepository.class);
  private final AzureNormalizer normalizer = new AzureNormalizer(resourceRepository);

  @Test
  void normalizeShouldReturnNormalizedMetric() {
    UUID subscriptionId = UUID.randomUUID();
    UUID resourceTableId = UUID.randomUUID();

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setProvider("AZURE");
    ingestionRecord.setSubscriptionId(subscriptionId.toString());
    ingestionRecord.setServiceName("Microsoft.Compute/virtualMachines");
    ingestionRecord.setResourceId(
        "/subscriptions/"
            + subscriptionId
            + "/resourceGroups/test/providers/Microsoft.Compute/virtualMachines/vm-1");
    ingestionRecord.setRegion("southafricanorth");
    ingestionRecord.setMetricName("Percentage CPU");
    ingestionRecord.setValue(75.0);
    ingestionRecord.setUnit("Percent");
    ingestionRecord.setTimestamp(Instant.parse("2026-09-03T10:00:00Z"));

    Resource resource = mock(Resource.class);
    when(resource.getId()).thenReturn(resourceTableId);

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            UUID.fromString(ingestionRecord.getSubscriptionId()),
            ingestionRecord.getServiceName(),
            ingestionRecord.getResourceId(),
            ingestionRecord.getRegion()))
        .thenReturn(Optional.of(resource));

    NormalizedMetric result = normalizer.normalize(ingestionRecord);

    assertNotNull(result);
    assertEquals(resourceTableId.toString(), result.getResourceId());
    assertEquals(subscriptionId.toString(), result.getAccountId());
    assertEquals("usage", result.getMetricType());
    assertEquals("Percentage CPU", result.getMetricName());
    assertEquals(75.0, result.getMetricValue());
    assertEquals("Percent", result.getUnit());
    assertNull(result.getCurrency());

    long expectedTimestamp = Instant.parse("2026-09-03T10:00:00Z").toEpochMilli();

    assertEquals(expectedTimestamp, result.getPeriodStart());
    assertEquals(expectedTimestamp, result.getPeriodEnd());

    verify(resourceRepository)
        .findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            subscriptionId,
            ingestionRecord.getServiceName(),
            ingestionRecord.getResourceId(),
            ingestionRecord.getRegion());
  }

  @Test
  void normalizeShouldReturnNullForNullRecord() {
    assertNull(normalizer.normalize(null));

    verifyNoInteractions(resourceRepository);
  }

  @Test
  void normalizeShouldHandleMissingResource() {
    UUID subscriptionId = UUID.randomUUID();

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setProvider("AZURE");
    ingestionRecord.setSubscriptionId(subscriptionId.toString());
    ingestionRecord.setServiceName("Microsoft.Compute/virtualMachines");
    ingestionRecord.setResourceId("vm-1");
    ingestionRecord.setRegion("southafricanorth");
    ingestionRecord.setMetricName("Percentage CPU");
    ingestionRecord.setValue(50.0);
    ingestionRecord.setUnit("Percent");

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            subscriptionId,
            ingestionRecord.getServiceName(),
            ingestionRecord.getResourceId(),
            ingestionRecord.getRegion()))
        .thenReturn(Optional.empty());

    NormalizedMetric result = normalizer.normalize(ingestionRecord);

    assertNotNull(result);
    assertNull(result.getResourceId());
    assertEquals(subscriptionId.toString(), result.getAccountId());
    assertEquals("usage", result.getMetricType());
    assertEquals("Percentage CPU", result.getMetricName());
    assertEquals(50.0, result.getMetricValue());
    assertEquals("Percent", result.getUnit());
    assertNull(result.getCurrency());
  }

  @Test
  void normalizeShouldClassifyCostMetric() {
    UUID subscriptionId = UUID.randomUUID();

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setSubscriptionId(subscriptionId.toString());
    ingestionRecord.setServiceName("Microsoft.Compute/virtualMachines");
    ingestionRecord.setResourceId("vm-1");
    ingestionRecord.setRegion("southafricanorth");
    ingestionRecord.setMetricName("Estimated Cost");
    ingestionRecord.setValue(123.45);
    ingestionRecord.setUnit("Currency");

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            subscriptionId,
            ingestionRecord.getServiceName(),
            ingestionRecord.getResourceId(),
            ingestionRecord.getRegion()))
        .thenReturn(Optional.empty());

    NormalizedMetric result = normalizer.normalize(ingestionRecord);

    assertNotNull(result);
    assertEquals("cost", result.getMetricType());
    assertEquals("Estimated Cost", result.getMetricName());
    assertEquals(123.45, result.getMetricValue());
    assertEquals("Currency", result.getUnit());
    assertEquals("ZAR", result.getCurrency());
  }

  @Test
  void normalizeShouldClassifyPerformanceMetric() {
    UUID subscriptionId = UUID.randomUUID();

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setSubscriptionId(subscriptionId.toString());
    ingestionRecord.setServiceName("Microsoft.Compute/virtualMachines");
    ingestionRecord.setResourceId("vm-1");
    ingestionRecord.setRegion("southafricanorth");
    ingestionRecord.setMetricName("Network Latency");
    ingestionRecord.setValue(25.0);
    ingestionRecord.setUnit("Milliseconds");

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            subscriptionId,
            ingestionRecord.getServiceName(),
            ingestionRecord.getResourceId(),
            ingestionRecord.getRegion()))
        .thenReturn(Optional.empty());

    NormalizedMetric result = normalizer.normalize(ingestionRecord);

    assertNotNull(result);
    assertEquals("performance", result.getMetricType());
    assertEquals("Network Latency", result.getMetricName());
    assertNull(result.getCurrency());
  }

  @Test
  void normalizeShouldUseUnknownMetricNameWhenMetricNameIsNull() {
    UUID subscriptionId = UUID.randomUUID();

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setSubscriptionId(subscriptionId.toString());
    ingestionRecord.setServiceName("Microsoft.Compute/virtualMachines");
    ingestionRecord.setResourceId("vm-1");
    ingestionRecord.setRegion("southafricanorth");
    ingestionRecord.setMetricName(null);
    ingestionRecord.setValue(10.0);
    ingestionRecord.setUnit("Count");

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            subscriptionId,
            ingestionRecord.getServiceName(),
            ingestionRecord.getResourceId(),
            ingestionRecord.getRegion()))
        .thenReturn(Optional.empty());

    NormalizedMetric result = normalizer.normalize(ingestionRecord);

    assertNotNull(result);
    assertEquals("unknown", result.getMetricName());
    assertEquals("usage", result.getMetricType());
    assertNull(result.getCurrency());
  }

  @Test
  void normalizeShouldUsePeriodStartAndEndWhenProvided() {
    UUID subscriptionId = UUID.randomUUID();

    Instant periodStart = Instant.parse("2026-09-03T10:00:00Z");
    Instant periodEnd = Instant.parse("2026-09-03T10:05:00Z");

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setSubscriptionId(subscriptionId.toString());
    ingestionRecord.setServiceName("Microsoft.Compute/virtualMachines");
    ingestionRecord.setResourceId("vm-1");
    ingestionRecord.setRegion("southafricanorth");
    ingestionRecord.setMetricName("Percentage CPU");
    ingestionRecord.setValue(80.0);
    ingestionRecord.setUnit("Percent");
    ingestionRecord.setTimestamp(Instant.parse("2026-09-03T11:00:00Z"));
    ingestionRecord.setPeriodStart(periodStart);
    ingestionRecord.setPeriodEnd(periodEnd);

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            subscriptionId,
            ingestionRecord.getServiceName(),
            ingestionRecord.getResourceId(),
            ingestionRecord.getRegion()))
        .thenReturn(Optional.empty());

    NormalizedMetric result = normalizer.normalize(ingestionRecord);

    assertNotNull(result);
    assertEquals(periodStart.toEpochMilli(), result.getPeriodStart());
    assertEquals(periodEnd.toEpochMilli(), result.getPeriodEnd());
  }

  @Test
  void normalizeShouldFallbackToTimestampWhenPeriodStartAndEndAreNull() {
    UUID subscriptionId = UUID.randomUUID();

    Instant timestamp = Instant.parse("2026-09-03T12:00:00Z");

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setSubscriptionId(subscriptionId.toString());
    ingestionRecord.setServiceName("Microsoft.Compute/virtualMachines");
    ingestionRecord.setResourceId("vm-1");
    ingestionRecord.setRegion("southafricanorth");
    ingestionRecord.setMetricName("Percentage CPU");
    ingestionRecord.setValue(80.0);
    ingestionRecord.setUnit("Percent");
    ingestionRecord.setTimestamp(timestamp);

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            subscriptionId,
            ingestionRecord.getServiceName(),
            ingestionRecord.getResourceId(),
            ingestionRecord.getRegion()))
        .thenReturn(Optional.empty());

    NormalizedMetric result = normalizer.normalize(ingestionRecord);

    assertNotNull(result);
    assertEquals(timestamp.toEpochMilli(), result.getPeriodStart());
    assertEquals(timestamp.toEpochMilli(), result.getPeriodEnd());
  }

  @Test
  void normalizeShouldUseZeroPeriodsWhenTimestampAndPeriodsAreNull() {
    UUID subscriptionId = UUID.randomUUID();

    UsageRecordModel ingestionRecord = new UsageRecordModel();
    ingestionRecord.setSubscriptionId(subscriptionId.toString());
    ingestionRecord.setServiceName("Microsoft.Compute/virtualMachines");
    ingestionRecord.setResourceId("vm-1");
    ingestionRecord.setRegion("southafricanorth");
    ingestionRecord.setMetricName("Percentage CPU");
    ingestionRecord.setValue(80.0);
    ingestionRecord.setUnit("Percent");

    when(resourceRepository.findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
            subscriptionId,
            ingestionRecord.getServiceName(),
            ingestionRecord.getResourceId(),
            ingestionRecord.getRegion()))
        .thenReturn(Optional.empty());

    NormalizedMetric result = normalizer.normalize(ingestionRecord);

    assertNotNull(result);
    assertEquals(0L, result.getPeriodStart());
    assertEquals(0L, result.getPeriodEnd());
  }
}
