package com.cloudsherpa.service.dashboard.dto;

import java.util.UUID;

public record WidgetDTO(
    UUID id,
    String type,
    String displayName,
    Integer startX,
    Integer startY,
    Integer width,
    Integer height,
    UUID resourceId,
    String metricType) {}
