package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import java.util.UUID;

public record AddChartWidgetToolDto(
    String displayName,
    UUID resourceId,
    String metricName,
    String chartType,
    String chartColour,
    Integer startX,
    Integer startY,
    Integer width,
    Integer height) {}
