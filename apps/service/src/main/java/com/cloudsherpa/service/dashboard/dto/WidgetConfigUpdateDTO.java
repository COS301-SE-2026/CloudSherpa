package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.TypeEnum;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "widgetType",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ChartWidgetConfigUpdateDTO.class, name = "CHART"),
  @JsonSubTypes.Type(value = KpiWidgetConfigUpdateDTO.class, name = "KPI")
})
public sealed interface WidgetConfigUpdateDTO
    permits ChartWidgetConfigUpdateDTO, KpiWidgetConfigUpdateDTO {
  UUID id();

  TypeEnum widgetType();

  String displayName();

  default UUID resourceId() {
    return null;
  }

  default String metricType() {
    return null;
  }

  default List<String> chargeIds() {
    return List.of();
  }

  default Integer aggregationWindowDays() {
    return null;
  }
}
