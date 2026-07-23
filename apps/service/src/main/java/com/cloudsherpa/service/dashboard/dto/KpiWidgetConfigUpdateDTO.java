package com.cloudsherpa.service.dashboard.dto;

import java.util.List;
import java.util.UUID;

public record KpiWidgetConfigUpdateDTO(
    UUID id,
    String widgetType,
    String displayName,
    List<String> chargeIds,
    Integer aggregationWindowDays)
    implements WidgetConfigUpdateDTO {}
