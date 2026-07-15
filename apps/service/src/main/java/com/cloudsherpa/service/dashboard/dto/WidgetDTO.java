package com.cloudsherpa.service.dashboard.dto;

import java.util.UUID;

public class WidgetDTO {
  private UUID id;
  private String type;
  private String displayName;

  private Integer startX;
  private Integer startY;
  private Integer width;
  private Integer height;

  private UUID resourceId;
  private String metricType;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Integer getStartX() {
    return startX;
  }

  public void setStartX(Integer startX) {
    this.startX = startX;
  }

  public Integer getStartY() {
    return startY;
  }

  public void setStartY(Integer startY) {
    this.startY = startY;
  }

  public Integer getWidth() {
    return width;
  }

  public void setWidth(Integer width) {
    this.width = width;
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = height;
  }

  public UUID getResourceId() {
    return resourceId;
  }

  public void setResourceId(UUID resourceId) {
    this.resourceId = resourceId;
  }

  public String getMetricType() {
    return metricType;
  }

  public void setMetricType(String metricType) {
    this.metricType = metricType;
  }
}
