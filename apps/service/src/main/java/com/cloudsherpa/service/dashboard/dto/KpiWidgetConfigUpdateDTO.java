package com.cloudsherpa.service.dashboard.dto;

import java.util.List;
import java.util.UUID;

public record KpiWidgetConfigUpdateDTO(
    UUID id,
    String widgetType,
    String displayName,
    List<UUID> chargeIds,
    Integer aggregationWindowDays)
    implements WidgetConfigUpdateDTO {
  @Override
  public WidgetConfigUpdateDTO withUserId(UUID userId) {
    return new KpiWidgetConfigUpdateDTO(
        userId, widgetType, displayName, chargeIds, aggregationWindowDays);
  }
}
