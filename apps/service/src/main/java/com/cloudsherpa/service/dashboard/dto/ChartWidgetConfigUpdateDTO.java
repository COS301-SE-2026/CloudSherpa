package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.ChartTypeEnum;
import java.util.UUID;

public record ChartWidgetConfigUpdateDTO(
    UUID id,
    String widgetType,
    String displayName,
    ChartTypeEnum chartType,
    UUID resourceId,
    String metricType)
    implements WidgetConfigUpdateDTO {
  @Override
  public WidgetConfigUpdateDTO withUserId(UUID userId) {
    return new ChartWidgetConfigUpdateDTO(
        userId, widgetType, displayName, chartType, resourceId, metricType);
  }
}
