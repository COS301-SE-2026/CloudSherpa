package com.cloudsherpa.service.dashboard.dto;

import java.util.UUID;

public sealed interface WidgetDTO permits KpiWidgetDTO, ChartWidgetDTO {
  UUID userId();

  UUID id();

  String displayName();

  Integer startX();

  Integer startY();

  Integer width();

  Integer height();

  WidgetDTO withUserId(UUID userId);
}
