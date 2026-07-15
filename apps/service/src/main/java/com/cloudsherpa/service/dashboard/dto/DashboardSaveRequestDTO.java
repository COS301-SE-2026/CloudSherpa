package com.cloudsherpa.service.dashboard.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class DashboardSaveRequestDTO {

  private UUID id;
  private String name;
  private OffsetDateTime timeFrom;
  private OffsetDateTime timeTo;
  private String predefinedTime;
  private List<WidgetDTO> widgets;

  // Getters and Setters...
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OffsetDateTime getTimeFrom() {
    return timeFrom;
  }

  public void setTimeFrom(OffsetDateTime timeFrom) {
    this.timeFrom = timeFrom;
  }

  public OffsetDateTime getTimeTo() {
    return timeTo;
  }

  public void setTimeTo(OffsetDateTime timeTo) {
    this.timeTo = timeTo;
  }

  public String getPredefinedTime() {
    return predefinedTime;
  }

  public void setPredefinedTime(String predefinedTime) {
    this.predefinedTime = predefinedTime;
  }

  public List<WidgetDTO> getWidgets() {
    return widgets;
  }

  public void setWidgets(List<WidgetDTO> widgets) {
    this.widgets = widgets;
  }
}
