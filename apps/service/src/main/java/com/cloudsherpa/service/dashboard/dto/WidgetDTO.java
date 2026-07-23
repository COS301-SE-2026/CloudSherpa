package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.TypeEnum;
import java.util.UUID;

public sealed interface WidgetDTO permits KpiWidgetDTO, ChartWidgetDTO {
  UUID id();

  TypeEnum widgetType();

  String displayName();

  Integer startX();

  Integer startY();

  Integer width();

  Integer height();
}
