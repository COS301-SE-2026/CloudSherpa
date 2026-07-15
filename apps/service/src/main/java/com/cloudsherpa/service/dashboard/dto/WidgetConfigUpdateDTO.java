package com.cloudsherpa.service.dashboard.dto;

import java.util.UUID;

public record WidgetConfigUpdateDTO(
    String type, String displayName, UUID resourceId, String metricType) {}
