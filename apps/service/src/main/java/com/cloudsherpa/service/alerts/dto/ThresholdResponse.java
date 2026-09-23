package com.cloudsherpa.service.alerts.dto;

import com.cloudsherpa.lib.entities.AlertSeverityEnum;
import com.cloudsherpa.lib.entities.Threshold;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ThresholdResponse(
    UUID thresholdId,
    UUID resourceId,
    UUID userId,
    String metricName,
    String operator,
    double value,
    AlertSeverityEnum severity,
    boolean enabled,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {

  public static ThresholdResponse from(Threshold threshold, String displayMetricName) {
    return new ThresholdResponse(
        threshold.getThresholdId(),
        threshold.getResourceId(),
        threshold.getUserId(),
        displayMetricName,
        threshold.getOperator(),
        threshold.getValue(),
        threshold.getSeverity(),
        threshold.isEnabled(),
        threshold.getCreatedAt(),
        threshold.getUpdatedAt());
  }
}
