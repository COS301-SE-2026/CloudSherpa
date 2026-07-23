package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.TypeEnum;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "widgetType",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ChartWidgetDTO.class, name = "CHART"),
  @JsonSubTypes.Type(value = KpiWidgetDTO.class, name = "KPI")
})
public sealed interface WidgetDTO permits KpiWidgetDTO, ChartWidgetDTO {
  UUID id();

  TypeEnum widgetType();

  String displayName();

  Integer startX();

  Integer startY();

  Integer width();

  Integer height();
}
