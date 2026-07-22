package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.ChartTypeEnum;
import java.util.UUID;

public record ChartWidgetDTO(
    UUID userId,
    UUID id,
    String widgetType,
    String displayName,
    Integer startX,
    Integer startY,
    Integer width,
    Integer height,
    ChartTypeEnum chartType,
    UUID resourceId,
    String metricType)
    implements WidgetDTO {
  public WidgetDTO withUserId(UUID userId) {
    return new ChartWidgetDTO(
        userId,
        id,
        widgetType,
        displayName,
        startX,
        startY,
        width,
        height,
        chartType,
        resourceId,
        metricType);
  }
}
