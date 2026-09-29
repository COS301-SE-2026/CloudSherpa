package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import java.util.List;
import java.util.UUID;

public record UpdateKpiWidgetToolDto(
    UUID widgetId, String displayName, List<String> chargeIds, Integer aggregationWindowDays) {}
