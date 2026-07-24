package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.ChartTypeEnum;
import com.cloudsherpa.lib.entities.TypeEnum;
import java.util.UUID;

public record ChartWidgetConfigUpdateDTO(
    UUID id,
    TypeEnum widgetType,
    String displayName,
    ChartTypeEnum chartType,
    UUID resourceId,
    String metricType)
    implements WidgetConfigUpdateDTO {}
