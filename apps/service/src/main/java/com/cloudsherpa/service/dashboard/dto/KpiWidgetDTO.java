package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.TypeEnum;
import java.util.List;
import java.util.UUID;

public record KpiWidgetDTO(
    UUID userId,
    UUID id,
    TypeEnum widgetType,
    String displayName,
    Integer startX,
    Integer startY,
    Integer width,
    Integer height,
    List<String> chargeIds,
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
