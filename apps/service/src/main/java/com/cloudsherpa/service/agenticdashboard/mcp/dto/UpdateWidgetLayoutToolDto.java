package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import java.util.UUID;

public record UpdateWidgetLayoutToolDto(
    UUID widgetId, Integer startX, Integer startY, Integer width, Integer height) {}
