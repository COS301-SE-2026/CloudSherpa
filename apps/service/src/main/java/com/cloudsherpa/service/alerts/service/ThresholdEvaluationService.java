package com.cloudsherpa.service.alerts.service;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.ChartResource;
import com.cloudsherpa.lib.entities.WidgetThreshold;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.lib.repositories.ChartResourceRepository;
import com.cloudsherpa.lib.repositories.WidgetThresholdRepository;
import com.cloudsherpa.service.listener.dto.MetricStreamEventDto;
import com.cloudsherpa.service.sse.SseService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
  private static final String ALERT_STATUS_ACTIVE = "ACTIVE";
  private static final String ALERT_TYPE_THRESHOLD = "THRESHOLD";

  private final ChartResourceRepository chartResourceRepository;
  private final WidgetThresholdRepository widgetThresholdRepository;
  private final AlertRepository alertRepository;
  private final SseService sseService;

  public ThresholdEvaluationService(
      ChartResourceRepository chartResourceRepository,
      WidgetThresholdRepository widgetThresholdRepository,
      AlertRepository alertRepository,
      SseService sseService) {
    this.chartResourceRepository = chartResourceRepository;
    this.widgetThresholdRepository = widgetThresholdRepository;
    this.alertRepository = alertRepository;
    this.sseService = sseService;
  }

  // resource-1 reports CPUUtilization, so matching widgets are evaluated.
  public void evaluate(MetricStreamEventDto event, UUID userId) {
    List<ChartResource> trackingWidgets =
        chartResourceRepository.findByResourceIdAndMetricName(
            event.resourceId(), event.metricName());

    if (trackingWidgets.isEmpty()) {
      return;
    }

    // ChartResource -> WidgetChart -> Widget provides the threshold owner.
    List<UUID> widgetIds = new ArrayList<>(trackingWidgets.size());

    for (ChartResource chartResource : trackingWidgets) {
      if (chartResource.getWidgetChart() != null) {
        widgetIds.add(chartResource.getWidgetChart().getWidgetId());
      }
    }

    for (UUID widgetId : widgetIds) {
      List<WidgetThreshold> thresholds =
          widgetThresholdRepository.findByWidgetIdAndMetricNameAndEnabledTrue(
              widgetId, event.metricName());

      for (WidgetThreshold threshold : thresholds) {
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
  }

  private void evaluateThreshold(
      WidgetThreshold threshold, MetricStreamEventDto event, UUID userId) {
    // CPUUtilization=92 with GT 80 creates an alert.
    if (event.metricValue() == null || !isViolated(threshold, event.metricValue())) {
      return;
    }

    // threshold:<threshold-id>:<resource-id>.
    String canonicalKey = buildCanonicalKey(threshold, event);

    Optional<Alert> existing =
        alertRepository.findByCanonicalKeyAndStatus(canonicalKey, ALERT_STATUS_ACTIVE);

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
  }

  private Alert buildNewAlert(
      WidgetThreshold threshold, MetricStreamEventDto event, UUID userId, String canonicalKey) {
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
        .widgetId(threshold.getWidgetId())
        .alertType(ALERT_TYPE_THRESHOLD)
        .severity(Optional.ofNullable(threshold.getSeverity()).orElse("WARNING"))
        .title(buildTitle(threshold, event))
        .message(buildMessage(threshold, event))
        .payload(payload)
        .status(ALERT_STATUS_ACTIVE)
        .canonicalKey(canonicalKey)
        .createdAt(now)
        .lastSeen(now)
        .build();
  }

  private boolean isViolated(WidgetThreshold threshold, BigDecimal metricValue) {
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

  private String buildCanonicalKey(WidgetThreshold threshold, MetricStreamEventDto event) {
    return "threshold:" + threshold.getThresholdId() + ":" + event.resourceId();
  }

  private String buildTitle(WidgetThreshold threshold, MetricStreamEventDto event) {
    return event.metricName() + " " + threshold.getOperator() + " " + threshold.getValue();
  }

  private String buildMessage(WidgetThreshold threshold, MetricStreamEventDto event) {
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
}
