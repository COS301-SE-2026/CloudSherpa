package com.cloudsherpa.service.alerts.service;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.AlertSeverityEnum;
import com.cloudsherpa.lib.entities.AlertStatusEnum;
import com.cloudsherpa.lib.entities.AlertTypeEnum;
import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.lib.repositories.OptimizationMetricStatisticsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.listener.dto.MetricStreamEventDto;
import com.cloudsherpa.service.sse.SseService;
import com.cloudsherpa.service.webhooks.events.alert.anomaly.AnomalyAlertPayload;
import com.cloudsherpa.service.webhooks.producers.WebhookProducerService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AnomalyEvaluationService {

  private static final Logger logger = LoggerFactory.getLogger(AnomalyEvaluationService.class);

  private static final double CRITICAL_Z_SCORE = 4.0;
  private static final double WARNING_Z_SCORE = 2.5;
  private static final int MIN_SAMPLE_SIZE = 30;

  private final OptimizationMetricStatisticsRepository statisticsRepository;
  private final AlertRepository alertRepository;
  private final SseService sseService;
  private final WebhookProducerService webhookProducerService;
  private final ResourceRepository resourceRepository;

  public AnomalyEvaluationService(
      OptimizationMetricStatisticsRepository statisticsRepository,
      AlertRepository alertRepository,
      SseService sseService,
      WebhookProducerService webhookProducerService,
      ResourceRepository resourceRepository) {
    this.statisticsRepository = statisticsRepository;
    this.alertRepository = alertRepository;
    this.sseService = sseService;
    this.webhookProducerService = webhookProducerService;
    this.resourceRepository = resourceRepository;
  }

  public void evaluate(MetricStreamEventDto event, UUID userId) {
    if (event.metricValue() == null) {
      return;
    }

    Optional<OptimizationMetricStatistics> baseline =
        statisticsRepository.findFirstByResourceIdAndMetricNameOrderByWindowEndDesc(
            event.resourceId(), event.metricName());

    if (baseline.isEmpty()) {
      return;
    }

    try {
      evaluateAgainstBaseline(baseline.get(), event, userId);
    } catch (Exception exception) {
      logger.error(
          "Anomaly evaluation failed for resourceId={} metricName={}",
          event.resourceId(),
          event.metricName(),
          exception);
    }
  }

  private void evaluateAgainstBaseline(
      OptimizationMetricStatistics baseline, MetricStreamEventDto event, UUID userId) {
    BigDecimal standardDeviation = baseline.getStandardDeviation();
    BigDecimal average = baseline.getAverageValue();

    if (standardDeviation == null
        || average == null
        || standardDeviation.compareTo(BigDecimal.ZERO) <= 0
        || baseline.getSampleCount() == null
        || baseline.getSampleCount() < MIN_SAMPLE_SIZE) {
      return;
    }

    // Taken from
    // https://medium.com/@akashsri306/detecting-anomalies-with-z-scores-a-practical-approach-2f9a0f27458d
    // A threshold value is a predetermined limit or cutoff point that helps determine what is
    // considered an anomaly or outlier within a dataset.
    // It’s the point at which a Z-score is considered significant enough to label a data point as
    // unusual or different from the rest.
    double zScore =
        (event.metricValue().doubleValue() - average.doubleValue())
            / standardDeviation.doubleValue();

    AlertSeverityEnum severity = resolveSeverity(zScore);

    if (severity == null) {
      return;
    }

    String canonicalKey = buildCanonicalKey(event);

    Optional<Alert> existing =
        alertRepository.findByCanonicalKeyAndStatus(canonicalKey, AlertStatusEnum.ACTIVE);

    if (existing.isEmpty()
        && alertRepository
            .findByCanonicalKeyAndStatus(canonicalKey, AlertStatusEnum.DISABLED)
            .isPresent()) {
      // User disabled anomaly alerts for this metric+resource; don't recreate one.
      return;
    }

    Alert alert;
    if (existing.isPresent()) {
      // Repeated deviations update (and can upgrade the severity of) the existing alert.
      alert = existing.get();
      alert.setSeverity(severity);
      alert.setLastSeen(OffsetDateTime.now(ZoneOffset.UTC));

    } else {
      alert = buildNewAlert(baseline, event, userId, canonicalKey, severity, zScore);
    }

    alertRepository.save(alert);
    sseService.broadcast(userId, "alert", alert);

    // Build & submit webhook event
    Resource resource = resourceRepository.findById(event.resourceId()).orElseThrow();
    webhookProducerService.produceEvent(
        userId,
        resource.getAccountId(),
        "alert.anomaly",
        buildWebhookEventPayload(resource, baseline, event, zScore, severity, alert));
  }

  private AlertSeverityEnum resolveSeverity(double zScore) {
    double magnitude = Math.abs(zScore);

    // Taken from
    // https://medium.com/@akashsri306/detecting-anomalies-with-z-scores-a-practical-approach-2f9a0f27458d
    // Z-Score Greater than 2 (or Less than -2): This threshold suggests that data points with
    // Z-scores greater than 2 or less than -2 are considered unusual or outliers.
    // In other words, they are significantly different from the mean (average) of the dataset. This
    // threshold is often used in practice for detecting moderate outliers.

    // Z-Score Greater than 3 (or Less than -3): Using a threshold of Z-scores greater than 3 or
    // less than -3 is a stricter criterion for identifying outliers
    // Data points that exceed this threshold are considered highly unusual and are typically
    // reserved for identifying extreme outliers.

    if (magnitude >= CRITICAL_Z_SCORE) {
      return AlertSeverityEnum.CRITICAL;
    }
    if (magnitude >= WARNING_Z_SCORE) {
      return AlertSeverityEnum.WARNING;
    }
    return null;
  }

  private Alert buildNewAlert(
      OptimizationMetricStatistics baseline,
      MetricStreamEventDto event,
      UUID userId,
      String canonicalKey,
      AlertSeverityEnum severity,
      double zScore) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    Map<String, Object> payload =
        Map.of(
            "metric_name", event.metricName(),
            "metric_value", event.metricValue(),
            "average_value", baseline.getAverageValue(),
            "standard_deviation", baseline.getStandardDeviation(),
            "z_score", zScore,
            "resource_id", event.resourceId(),
            "period_start", event.periodStart(),
            "period_end", event.periodEnd());

    return Alert.builder()
        .userId(userId)
        .widgetId(null)
        .alertType(AlertTypeEnum.ANOMALY)
        .severity(severity)
        .title(buildTitle(event, zScore))
        .message(buildMessage(event, baseline, zScore))
        .payload(payload)
        .status(AlertStatusEnum.ACTIVE)
        .canonicalKey(canonicalKey)
        .createdAt(now)
        .lastSeen(now)
        .build();
  }

  private String buildCanonicalKey(MetricStreamEventDto event) {
    return "anomaly:" + event.resourceId() + ":" + event.metricName();
  }

  private String buildTitle(MetricStreamEventDto event, double zScore) {
    return event.metricName() + " anomaly detected (z=" + String.format("%.2f", zScore) + ")";
  }

  private String buildMessage(
      MetricStreamEventDto event, OptimizationMetricStatistics baseline, double zScore) {
    return event.metricName()
        + " is "
        + event.metricValue()
        + " (baseline average "
        + baseline.getAverageValue()
        + ", stddev "
        + baseline.getStandardDeviation()
        + ", z-score "
        + String.format("%.2f", zScore)
        + ") for resource "
        + event.resourceId();
  }

  private AnomalyAlertPayload buildWebhookEventPayload(
      Resource resource,
      OptimizationMetricStatistics baseline,
      MetricStreamEventDto event,
      double zScore,
      AlertSeverityEnum severity,
      Alert alert) {

    return new AnomalyAlertPayload(
        resource.getResourceIdentifier(),
        resource.getResourceName(),
        event.metricName(),
        baseline.getAverageValue(),
        baseline.getStandardDeviation(),
        zScore,
        severity,
        alert.getCreatedAt(),
        alert.getLastSeen());
  }
}
