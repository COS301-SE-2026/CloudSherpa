package com.cloudsherpa.service.dashboard.dto;

import java.util.UUID;

public record ChartWidgetConfigUpdateDTO(
    UUID userId,
    String widgetType,
    String displayName,
    String chartType,
    UUID resourceId,
    String metricType)
    implements WidgetConfigUpdateDTO {
  @Override
  public WidgetConfigUpdateDTO withUserId(UUID userId) {
    return new ChartWidgetConfigUpdateDTO(
        userId, widgetType, displayName, chartType, resourceId, metricType);
  }
}
