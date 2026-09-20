package com.cloudsherpa.service.alerts.dto;

import com.cloudsherpa.lib.entities.Alert;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record AlertResponse(
    UUID alertId,
    UUID userId,
    String alertType,
    String severity,
    String title,
    String message,
    Map<String, Object> payload,
    String status,
    String canonicalKey,
    OffsetDateTime createdAt,
    OffsetDateTime lastSeen,
    OffsetDateTime resolvedAt) {

  public static AlertResponse from(Alert alert) {
    return new AlertResponse(
        alert.getAlertId(),
        alert.getUserId(),
        alert.getAlertType(),
        alert.getSeverity(),
        alert.getTitle(),
        alert.getMessage(),
        alert.getPayload(),
        alert.getStatus(),
        alert.getCanonicalKey(),
        alert.getCreatedAt(),
        alert.getLastSeen(),
        alert.getResolvedAt());
  }
}
