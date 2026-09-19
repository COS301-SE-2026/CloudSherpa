package com.cloudsherpa.service.unit.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.Threshold;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.lib.repositories.ThresholdRepository;
import com.cloudsherpa.service.alerts.service.ThresholdEvaluationService;
import com.cloudsherpa.service.listener.dto.MetricStreamEventDto;
import com.cloudsherpa.service.sse.SseService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThresholdEvaluationServiceTest {

  @Mock private ThresholdRepository thresholdRepository;
  @Mock private AlertRepository alertRepository;
  @Mock private SseService sseService;

  private ThresholdEvaluationService service;
  private UUID userId;
  private UUID resourceId;

  @BeforeEach
  void setUp() {
    service = new ThresholdEvaluationService(thresholdRepository, alertRepository, sseService);
    userId = UUID.randomUUID();
    resourceId = UUID.randomUUID();
  }

  @Test
  void evaluateShouldCreateAlertWhenThresholdIsViolated() {
    Threshold threshold = threshold("CPUUtilization", "GT", 80.0, "HIGH", true);

    when(thresholdRepository.findByResourceIdAndMetricNameAndEnabledTrue(
            resourceId, "CPUUtilization"))
        .thenReturn(List.of(threshold));
    when(alertRepository.findByCanonicalKeyAndStatus(anyString(), eq("ACTIVE")))
        .thenReturn(Optional.empty());

    MetricStreamEventDto event = metricEvent("CPUUtilization", new BigDecimal(92));

    service.evaluate(event, userId);

    ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
    verify(alertRepository).save(captor.capture());

    Alert saved = captor.getValue();
    assertEquals("ACTIVE", saved.getStatus());
    assertEquals("THRESHOLD", saved.getAlertType());
    assertEquals("HIGH", saved.getSeverity());
    assertEquals("CPUUtilization GT 80.0", saved.getTitle());
    assertEquals(
        "CPUUtilization is 92 (threshold GT 80.0) for resource " + resourceId, saved.getMessage());
    assertEquals(
        "threshold:" + threshold.getThresholdId() + ":" + resourceId, saved.getCanonicalKey());
    verify(sseService).broadcast(eq(userId), eq("alert"), same(saved));
  }

  @Test
  void evaluateShouldSkipAlertWhenMetricDoesNotViolateThreshold() {
    Threshold threshold = threshold("CPUUtilization", "GT", 80.0, "HIGH", true);
    when(thresholdRepository.findByResourceIdAndMetricNameAndEnabledTrue(
            resourceId, "CPUUtilization"))
        .thenReturn(List.of(threshold));

    MetricStreamEventDto event = metricEvent("CPUUtilization", new BigDecimal(70));

    service.evaluate(event, userId);

    verify(alertRepository, never()).save(any());
    verify(sseService, never()).broadcast(any(), any(), any());
  }

  @Test
  void evaluateShouldReuseExistingActiveAlertForRepeatViolation() {
    Threshold threshold = threshold("CPUUtilization", "GT", 80.0, "HIGH", true);
    String canonicalKey = "threshold:" + threshold.getThresholdId() + ":" + resourceId;

    Alert existing =
        Alert.builder()
            .userId(userId)
            .widgetId(null)
            .alertType("THRESHOLD")
            .severity("HIGH")
            .title("CPUUtilization GT 80.0")
            .message("CPUUtilization is 92 (threshold GT 80.0) for resource " + resourceId)
            .payload(Map.of("metric_name", "CPUUtilization"))
            .status("ACTIVE")
            .canonicalKey(canonicalKey)
            .createdAt(OffsetDateTime.now().minusMinutes(5))
            .lastSeen(OffsetDateTime.now().minusMinutes(5))
            .build();

    when(thresholdRepository.findByResourceIdAndMetricNameAndEnabledTrue(
            resourceId, "CPUUtilization"))
        .thenReturn(List.of(threshold));
    when(alertRepository.findByCanonicalKeyAndStatus(canonicalKey, "ACTIVE"))
        .thenReturn(Optional.of(existing));

    MetricStreamEventDto event = metricEvent("CPUUtilization", new BigDecimal(92));

    service.evaluate(event, userId);

    assertNotNull(existing.getLastSeen());
    assertTrue(existing.getLastSeen().isAfter(OffsetDateTime.now().minusMinutes(1)));
    verify(alertRepository).save(existing);
    verify(sseService).broadcast(eq(userId), eq("alert"), same(existing));
  }

  @Test
  void evaluateShouldIgnoreUnknownOperatorAndNotCreateAlert() {
    Threshold threshold = threshold("CPUUtilization", "UNKNOWN", 80.0, "HIGH", true);
    when(thresholdRepository.findByResourceIdAndMetricNameAndEnabledTrue(
            resourceId, "CPUUtilization"))
        .thenReturn(List.of(threshold));

    MetricStreamEventDto event = metricEvent("CPUUtilization", new BigDecimal(92));

    service.evaluate(event, userId);

    verify(alertRepository, never()).save(any());
    verify(sseService, never()).broadcast(any(), any(), any());
  }

  private Threshold threshold(
      String metricName, String operator, double value, String severity, boolean enabled) {
    Threshold threshold =
        new Threshold(resourceId, userId, metricName, operator, value, severity, enabled);
    threshold.setThresholdId(UUID.randomUUID());
    return threshold;
  }

  private MetricStreamEventDto metricEvent(String metricName, BigDecimal metricValue) {
    OffsetDateTime now = OffsetDateTime.now();
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
