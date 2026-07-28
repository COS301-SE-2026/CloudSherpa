package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.TypeEnum;
import java.util.List;
import java.util.UUID;

public record KpiWidgetConfigUpdateDTO(
    UUID id,
    TypeEnum widgetType,
    String displayName,
    List<String> chargeIds,
    Integer aggregationWindowDays)
    implements WidgetConfigUpdateDTO {}
