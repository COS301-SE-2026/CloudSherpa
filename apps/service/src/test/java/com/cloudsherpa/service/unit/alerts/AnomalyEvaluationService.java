package com.cloudsherpa.service.unit.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.AlertSeverityEnum;
import com.cloudsherpa.lib.entities.AlertStatusEnum;
import com.cloudsherpa.lib.entities.AlertTypeEnum;
import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.lib.repositories.OptimizationMetricStatisticsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.alerts.service.AnomalyEvaluationService;
import com.cloudsherpa.service.listener.dto.MetricStreamEventDto;
import com.cloudsherpa.service.sse.SseService;
import com.cloudsherpa.service.webhooks.producers.WebhookProducerService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnomalyEvaluationServiceTest {

  @Mock private OptimizationMetricStatisticsRepository statisticsRepository;
  @Mock private AlertRepository alertRepository;
  @Mock private SseService sseService;
  @Mock private WebhookProducerService webhookProducerService;
  @Mock private ResourceRepository resourceRepository;

  private AnomalyEvaluationService service;
  private UUID userId;
  private UUID resourceId;

  @BeforeEach
  void setUp() {
    service =
        new AnomalyEvaluationService(
            statisticsRepository,
            alertRepository,
            sseService,
            webhookProducerService,
            resourceRepository);
    userId = UUID.randomUUID();
    resourceId = UUID.randomUUID();
  }

  @Test
  void evaluateShouldCreateWarningAlertWhenZScoreIsModerate() {
    OptimizationMetricStatistics baseline =
        mockBaseline(new BigDecimal(50.0), new BigDecimal(10.0), 35);
    mockResourceRetrieval();

    when(statisticsRepository.findFirstByResourceIdAndMetricNameOrderByWindowEndDesc(
            resourceId, "ResponseTime"))
        .thenReturn(Optional.of(baseline));
    when(alertRepository.findByCanonicalKeyAndStatus(anyString(), eq(AlertStatusEnum.ACTIVE)))
        .thenReturn(Optional.empty());

    MetricStreamEventDto event = metricEvent("ResponseTime", new BigDecimal(80.0));

    service.evaluate(event, userId);

    ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
    verify(alertRepository).save(captor.capture());

    Alert saved = captor.getValue();

    assertEquals(AlertStatusEnum.ACTIVE, saved.getStatus());
    assertEquals(AlertTypeEnum.ANOMALY, saved.getAlertType());
    assertEquals(AlertSeverityEnum.WARNING, saved.getSeverity());

    assertTrue(saved.getTitle().contains(String.format("anomaly detected (z=%.2f)", 3.0)));
    assertEquals("anomaly:" + resourceId + ":ResponseTime", saved.getCanonicalKey());

    verify(sseService).broadcast(eq(userId), eq("alert"), same(saved));
    verify(webhookProducerService).produceEvent(eq(userId), any(), eq("alert.anomaly"), any());
  }

  @Test
  void evaluateShouldCreateCriticalAlertWhenZScoreIsExtreme() {
    OptimizationMetricStatistics baseline =
        mockBaseline(new BigDecimal(50.0), new BigDecimal(10.0), 35);
    mockResourceRetrieval();

    when(statisticsRepository.findFirstByResourceIdAndMetricNameOrderByWindowEndDesc(
            resourceId, "ResponseTime"))
        .thenReturn(Optional.of(baseline));
    when(alertRepository.findByCanonicalKeyAndStatus(anyString(), eq(AlertStatusEnum.ACTIVE)))
        .thenReturn(Optional.empty());

    MetricStreamEventDto event = metricEvent("ResponseTime", new BigDecimal(95.0));

    service.evaluate(event, userId);

    ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
    verify(alertRepository).save(captor.capture());

    assertEquals(AlertSeverityEnum.CRITICAL, captor.getValue().getSeverity());
    assertTrue(
        captor.getValue().getTitle().contains(String.format("anomaly detected (z=%.2f)", 4.5)));
  }

  @Test
  void evaluateShouldSkipAlertWhenZScoreIsNormal() {
    OptimizationMetricStatistics baseline =
        mockBaseline(new BigDecimal(50.0), new BigDecimal(10.0), 35);

    when(statisticsRepository.findFirstByResourceIdAndMetricNameOrderByWindowEndDesc(
            resourceId, "ResponseTime"))
        .thenReturn(Optional.of(baseline));

    MetricStreamEventDto event = metricEvent("ResponseTime", new BigDecimal(60.0));

    service.evaluate(event, userId);

    verify(alertRepository, never()).save(any());
    verify(sseService, never()).broadcast(any(), any(), any());
  }

  @Test
  void evaluateShouldReuseAndUpgradeExistingActiveAlert() {
    OptimizationMetricStatistics baseline =
        mockBaseline(new BigDecimal(50.0), new BigDecimal(10.0), 35);
    String canonicalKey = "anomaly:" + resourceId + ":ResponseTime";
    mockResourceRetrieval();

    Alert existing =
        Alert.builder()
            .userId(userId)
            .alertType(AlertTypeEnum.ANOMALY)
            .severity(AlertSeverityEnum.WARNING)
            .status(AlertStatusEnum.ACTIVE)
            .canonicalKey(canonicalKey)
            .createdAt(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5))
            .build();

    when(statisticsRepository.findFirstByResourceIdAndMetricNameOrderByWindowEndDesc(
            resourceId, "ResponseTime"))
        .thenReturn(Optional.of(baseline));
    when(alertRepository.findByCanonicalKeyAndStatus(canonicalKey, AlertStatusEnum.ACTIVE))
        .thenReturn(Optional.of(existing));

    MetricStreamEventDto event = metricEvent("ResponseTime", new BigDecimal(95.0));

    service.evaluate(event, userId);

    assertEquals(AlertSeverityEnum.CRITICAL, existing.getSeverity());
    assertNotNull(existing.getLastSeen());

    verify(alertRepository).save(existing);
    verify(sseService).broadcast(eq(userId), eq("alert"), same(existing));
  }

  @Test
  void evaluateShouldSkipWhenSampleSizeIsInsufficient() {
    OptimizationMetricStatistics baseline =
        mockBaseline(new BigDecimal(50.0), new BigDecimal(10.0), 20);

    when(statisticsRepository.findFirstByResourceIdAndMetricNameOrderByWindowEndDesc(
            resourceId, "ResponseTime"))
        .thenReturn(Optional.of(baseline));

    MetricStreamEventDto event = metricEvent("ResponseTime", new BigDecimal(95.0));
    service.evaluate(event, userId);

    verify(alertRepository, never()).save(any());
  }

  private OptimizationMetricStatistics mockBaseline(
      BigDecimal average, BigDecimal stdDev, Integer sampleCount) {
    OptimizationMetricStatistics stats = mock(OptimizationMetricStatistics.class);

    when(stats.getAverageValue()).thenReturn(average);
    when(stats.getStandardDeviation()).thenReturn(stdDev);
    when(stats.getSampleCount()).thenReturn(sampleCount);

    return stats;
  }

  private void mockResourceRetrieval() {
    Resource resource = mock(Resource.class);

    when(resource.getAccountId()).thenReturn(UUID.randomUUID());
    when(resource.getResourceIdentifier()).thenReturn("provider-resource-id");
    when(resource.getResourceName()).thenReturn("Example resource");
    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
  }

  private MetricStreamEventDto metricEvent(String metricName, BigDecimal metricValue) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    return new MetricStreamEventDto(
        UUID.randomUUID(),
        "USD",
        resourceId,
        "cpu",
        metricName,
        metricValue,
        now.minusMinutes(5),
        now,
        now,
        "percent");
  }
}
