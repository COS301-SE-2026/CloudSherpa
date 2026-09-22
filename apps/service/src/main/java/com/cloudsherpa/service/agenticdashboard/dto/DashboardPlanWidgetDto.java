package com.cloudsherpa.service.agenticdashboard.dto;

import com.cloudsherpa.lib.entities.ChartColourEnum;
import com.cloudsherpa.lib.entities.ChartTypeEnum;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.TypeEnum;
import java.util.List;
import java.util.UUID;

public record DashboardPlanWidgetDto(
    UUID widgetId,
    TypeEnum widgetType,
    String displayName,
    Integer startX,
    Integer startY,
    Integer width,
    Integer height,
    ChartTypeEnum chartType,
    ChartColourEnum chartColour,
    ProviderEnum provider,
    String title,
    UUID accountId,
    UUID resourceId,
    String metricType,
    String metricName,
    List<String> chargeIds,
    Integer aggregationWindowDays) {}
