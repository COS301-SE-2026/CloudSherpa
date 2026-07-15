package com.cloudsherpa.service.dashboard.dto;

import java.util.UUID;

public record WidgetDTO(
    UUID userId,
    UUID id,
    String type,
    String displayName,
    Integer startX,
    Integer startY,
    Integer width,
    Integer height,
    UUID resourceId,
    String metricType) {
  public WidgetDTO withUserId(UUID userId) {
    return new WidgetDTO(
        userId, id, type, displayName, startX, startY, width, height, resourceId, metricType);
  }
}
