package com.cloudsherpa.service.dashboard.dto;

import java.util.List;
import java.util.UUID;

public record KpiWidgetDTO(
    UUID userId,
    UUID id,
    String widgetType,
    String displayName,
    Integer startX,
    Integer startY,
    Integer width,
    Integer height,
    List<UUID> chargeIds,
    Integer aggregationWindowDays)
    implements WidgetDTO {
  public WidgetDTO withUserId(UUID userId) {
    return new KpiWidgetDTO(
        userId,
        id,
        widgetType,
        displayName,
        startX,
        startY,
        width,
        height,
        chargeIds,
        aggregationWindowDays);
  }
}
