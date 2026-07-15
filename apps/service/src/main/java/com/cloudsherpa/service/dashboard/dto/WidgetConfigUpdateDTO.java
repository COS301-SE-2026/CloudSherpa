package com.cloudsherpa.service.dashboard.dto;

import java.util.UUID;

public record WidgetConfigUpdateDTO(
    UUID userId, String type, String displayName, UUID resourceId, String metricType) {
  public WidgetConfigUpdateDTO withUserId(UUID userId) {
    return new WidgetConfigUpdateDTO(userId, type, displayName, resourceId, metricType);
  }
}
