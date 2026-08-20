package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.ChartTypeEnum;
import com.cloudsherpa.lib.entities.TypeEnum;
import java.util.UUID;

public record ChartWidgetDTO(
    UUID id,
    TypeEnum widgetType,
    String displayName,
    Integer startX,
    Integer startY,
    Integer width,
    Integer height,
    ChartTypeEnum chartType,
    UUID resourceId,
    String metricType,
    String metricDisplayName)
    implements WidgetDTO {}
