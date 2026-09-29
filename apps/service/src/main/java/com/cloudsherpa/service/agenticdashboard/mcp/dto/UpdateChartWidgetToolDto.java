package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import java.util.UUID;

public record UpdateChartWidgetToolDto(
    UUID widgetId,
    String displayName,
    String chartType,
    String chartColour,
    UUID resourceId,
    String metricName) {}
