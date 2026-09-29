package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import java.util.List;

public record AddKpiWidgetToolDto(
    String displayName,
    List<String> chargeIds,
    Integer aggregationWindowDays,
    Integer startX,
    Integer startY,
    Integer width,
    Integer height) {}
