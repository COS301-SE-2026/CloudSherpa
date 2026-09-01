package com.cloudsherpa.ingestion.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.normalization.model.NormalizedMetric;
import com.cloudsherpa.ingestion.service.CloudInfrastructureService;
import com.cloudsherpa.ingestion.service.SherpaDbPersistenceService;
import com.cloudsherpa.lib.entities.ChargeTypeEnum;
import com.cloudsherpa.lib.entities.CurrencyEnum;
import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.entities.NormalizedMetrics;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SherpaDbPersistenceServiceTest {

  @Mock private NormalizedMetricsRepository metricsRepo;
  @Mock private NormalizedCostsRepository normalizedCostsRepository;
  @Mock private CloudInfrastructureService infrastructureService;

  @Mock private EntityManager entityManager;
  @Mock private Query nativeQuery;

  @InjectMocks private SherpaDbPersistenceService service;

  @Captor private ArgumentCaptor<NormalizedMetrics> metricsCaptor;

  @Test
  void recordMetricShouldThrowWhenUserIdIsNull() {
    NormalizedMetric metric = mock(NormalizedMetric.class);
    UsageRecordModel r = mock(UsageRecordModel.class);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.recordMetric(metric, r, null));

    assertEquals("userId is required", exception.getMessage());
  }

  @Test
  void recordCostShouldThrowWhenUserIdIsNull() {
    NormalizedCosts costs = mock(NormalizedCosts.class);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> service.recordCost(costs, null));

    assertEquals("userId is required", exception.getMessage());
  }

  @Test
  void recordCostShouldUpsertWithCorrectSchemaAndExplicitCurrency() {
    UUID userId = UUID.randomUUID();
    String expectedSchema = "tenant_" + userId.toString().replace("-", "_");

    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);

    NormalizedCosts mockCosts = mock(NormalizedCosts.class);

    when(mockCosts.getCurrency()).thenReturn(CurrencyEnum.ZAR);
    when(mockCosts.getProvider()).thenReturn(ProviderEnum.AWS);
    when(mockCosts.getChargeType()).thenReturn(ChargeTypeEnum.Usage);

    service.recordCost(mockCosts, userId);

    verify(entityManager).createNativeQuery("SET search_path TO " + expectedSchema + ", public");
    verify(nativeQuery).executeUpdate();
    verify(normalizedCostsRepository)
        .upsert(mockCosts, "AWS", mockCosts.getChargeType().toString(), "ZAR");
  }

  @Test
  void recordCostShouldDefaultToUsdWhenCurrencyIsNull() {
    UUID userId = UUID.randomUUID();

    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);

    NormalizedCosts mockCosts = mock(NormalizedCosts.class);

    when(mockCosts.getCurrency()).thenReturn(null);
    when(mockCosts.getProvider()).thenReturn(ProviderEnum.GCP);
    when(mockCosts.getChargeType()).thenReturn(ChargeTypeEnum.Usage);

    service.recordCost(mockCosts, userId);

    verify(normalizedCostsRepository)
        .upsert(mockCosts, "GCP", mockCosts.getChargeType().toString(), "USD");
  }

  @Test
  void recordMetricShouldMapFieldsAndSaveEntity() {
    UUID userId = UUID.randomUUID();
    String expectedSchema = "tenant_" + userId.toString().replace("-", "_");

    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);

    UsageRecordModel r = mock(UsageRecordModel.class);
    Resource mockResource = mock(Resource.class);
    UUID resourceId = UUID.randomUUID();

    when(mockResource.getId()).thenReturn(resourceId);

    when(infrastructureService.ensureInfrastructure(r, userId)).thenReturn(mockResource);

    long startMillis = Instant.parse("2026-08-01T10:00:00Z").toEpochMilli();
    long endMillis = Instant.parse("2026-08-01T11:00:00Z").toEpochMilli();

    NormalizedMetric metric = mock(NormalizedMetric.class);

    when(metric.getPeriodStart()).thenReturn(startMillis);
    when(metric.getPeriodEnd()).thenReturn(endMillis);
    when(metric.getMetricType()).thenReturn("CPU");
    when(metric.getMetricName()).thenReturn("cpu_utilization");
    when(metric.getMetricValue()).thenReturn(75.5);
    when(metric.getUnit()).thenReturn("Percent");
    when(metric.getCurrency()).thenReturn("USD");

    service.recordMetric(metric, r, userId);

    verify(entityManager).createNativeQuery("SET search_path TO " + expectedSchema + ", public");

    verify(metricsRepo).save(metricsCaptor.capture());

    NormalizedMetrics savedEntity = metricsCaptor.getValue();

    assertEquals(resourceId, savedEntity.getResourceId());
    assertEquals("cpu", savedEntity.getMetricType());
    assertEquals("cpu_utilization", savedEntity.getMetricName());
    assertEquals(BigDecimal.valueOf(75.5), savedEntity.getMetricValue());
    assertEquals("Percent", savedEntity.getUnit());

    assertEquals(
        OffsetDateTime.ofInstant(Instant.ofEpochMilli(startMillis), ZoneOffset.UTC),
        savedEntity.getPeriodStart());
    assertEquals(
        OffsetDateTime.ofInstant(Instant.ofEpochMilli(endMillis), ZoneOffset.UTC),
        savedEntity.getPeriodEnd());
  }
}
