package com.cloudsherpa.service.alerts.service;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.AlertSeverityEnum;
import com.cloudsherpa.lib.entities.AlertStatusEnum;
import com.cloudsherpa.lib.entities.AlertTypeEnum;
import com.cloudsherpa.lib.entities.Threshold;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.lib.repositories.ThresholdRepository;
import com.cloudsherpa.service.listener.dto.MetricStreamEventDto;
import com.cloudsherpa.service.sse.SseService;
import com.cloudsherpa.service.webhooks.events.alert.threshold.ThresholdAlertPayload;
import com.cloudsherpa.service.webhooks.producers.WebhookProducerService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ThresholdEvaluationService {

  private static final Logger logger = LoggerFactory.getLogger(ThresholdEvaluationService.class);

  private final ThresholdRepository thresholdRepository;
  private final AlertRepository alertRepository;
  private final SseService sseService;
  private final WebhookProducerService webhookProducerService;

  public ThresholdEvaluationService(
      ThresholdRepository thresholdRepository,
      AlertRepository alertRepository,
      SseService sseService,
      WebhookProducerService webhookProducerService) {
    this.thresholdRepository = thresholdRepository;
    this.alertRepository = alertRepository;
    this.sseService = sseService;
    this.webhookProducerService = webhookProducerService;
  }

  // resource-1 reports CPUUtilization, so matching resource thresholds are evaluated.
  public void evaluate(MetricStreamEventDto event, UUID userId) {
    List<Threshold> thresholds =
        thresholdRepository.findByResourceIdAndMetricNameAndEnabledTrue(
            event.resourceId(), event.metricName());

    for (Threshold threshold : thresholds) {
      try {
        evaluateThreshold(threshold, event, userId);
      } catch (Exception exception) {
        // One failed threshold must not stop the remaining thresholds.
        logger.error(
            "Threshold evaluation failed for thresholdId={} resourceId={}",
            threshold.getThresholdId(),
            event.resourceId(),
            exception);
      }
    }
  }

  private void evaluateThreshold(Threshold threshold, MetricStreamEventDto event, UUID userId) {
    // CPUUtilization=92 with GT 80 creates an alert.
    if (event.metricValue() == null || !isViolated(threshold, event.metricValue())) {
      return;
    }

    // threshold:<threshold-id>:<resource-id>.
    String canonicalKey = buildCanonicalKey(threshold, event);

    Optional<Alert> existing =
        alertRepository.findByCanonicalKeyAndStatus(canonicalKey, AlertStatusEnum.ACTIVE);

    if (existing.isEmpty()
        && alertRepository
            .findByCanonicalKeyAndStatus(canonicalKey, AlertStatusEnum.DISABLED)
            .isPresent()) {
      // User disabled alerts for this exact metric+resource; don't recreate one.
      return;
    }

    Alert alert;
    if (existing.isPresent()) {
      // Repeated violations update the existing alert instead of creating duplicates.
      alert = existing.get();
      alert.setLastSeen(OffsetDateTime.now(ZoneOffset.UTC));
    } else {
      // The first violation creates a new active alert.
      alert = buildNewAlert(threshold, event, userId, canonicalKey);
    }

    alertRepository.save(alert);

    // Example SSE event: name="alert", data=the saved Alert object.
    sseService.broadcast(userId, "alert", alert);

    // Construct and submit webhook event
    webhookProducerService.produceEvent(
        userId,
        threshold.getResource().getAccountId(),
        "alert.threshold",
        buildWebhookEventPayload(threshold, event, alert));
  }

  private Alert buildNewAlert(
      Threshold threshold, MetricStreamEventDto event, UUID userId, String canonicalKey) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    // Example payload: metric_value=92, operator=GT, threshold_value=80.
    Map<String, Object> payload =
        Map.of(
            "metric_name", event.metricName(),
            "metric_value", event.metricValue(),
            "threshold_operator", threshold.getOperator(),
            "threshold_value", threshold.getValue(),
            "resource_id", event.resourceId(),
            "period_start", event.periodStart(),
            "period_end", event.periodEnd());

    return Alert.builder()
        .userId(userId)
        .widgetId(null)
        .alertType(AlertTypeEnum.THRESHOLD)
        .severity(Optional.ofNullable(threshold.getSeverity()).orElse(AlertSeverityEnum.WARNING))
        .title(buildTitle(threshold, event))
        .message(buildMessage(threshold, event))
        .payload(payload)
        .status(AlertStatusEnum.ACTIVE)
        .canonicalKey(canonicalKey)
        .createdAt(now)
        .lastSeen(now)
        .build();
  }

  private boolean isViolated(Threshold threshold, BigDecimal metricValue) {
    double value = metricValue.doubleValue();
    double thresholdValue = threshold.getValue();

    return switch (threshold.getOperator()) {
      case "GT" -> value > thresholdValue;
      case "GTE" -> value >= thresholdValue;
      case "LT" -> value < thresholdValue;
      case "LTE" -> value <= thresholdValue;
      case "EQ" -> value == thresholdValue;
      default -> {
        logger.warn("Unknown threshold operator: {}", threshold.getOperator());
        yield false;
      }
    };
  }

  private String buildCanonicalKey(Threshold threshold, MetricStreamEventDto event) {
    return "threshold:" + threshold.getThresholdId() + ":" + event.resourceId();
  }

  private String buildTitle(Threshold threshold, MetricStreamEventDto event) {
    return event.metricName() + " " + threshold.getOperator() + " " + threshold.getValue();
  }

  private String buildMessage(Threshold threshold, MetricStreamEventDto event) {
    return event.metricName()
        + " is "
        + event.metricValue()
        + " (threshold "
        + threshold.getOperator()
        + " "
        + threshold.getValue()
        + ") for resource "
        + event.resourceId();
  }

  private ThresholdAlertPayload buildWebhookEventPayload(
      Threshold threshold, MetricStreamEventDto event, Alert alert) {

    return new ThresholdAlertPayload(
        threshold.getResource().getResourceIdentifier(),
        threshold.getResource().getResourceName(),
        event.metricName(),
        event.metricValue(),
        event.unit(),
        threshold.getOperator(),
        threshold.getValue(),
        alert.getSeverity(),
        alert.getCreatedAt(),
        alert.getLastSeen());
  }
}
