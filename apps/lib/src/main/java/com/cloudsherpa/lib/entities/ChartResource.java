package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "chart_resource", schema = "public")
public class ChartResource {

  @Id
  @Column(name = "chart_resource_id", nullable = false)
  private UUID id;

  @Column(name = "chart_id", nullable = false)
  private UUID widgetChartId;

  @ManyToOne
  @JoinColumn(name = "widget_id", nullable = false, insertable = false, updatable = false)
  private Widget widget;

  @Column(name = "resource_id", nullable = false)
  private UUID resourceId;

  @Column(name = "metric_type", nullable = false, length = 50)
  private String metricType;

  protected ChartResource() {}

  public ChartResource(UUID id, UUID widgetChartId, UUID resourceId, String metricType) {
    this.id = id;
    this.widgetChartId = widgetChartId;
    this.resourceId = resourceId;
    this.metricType = metricType;
  }

  public UUID getId() {
    return id;
  }

  public UUID getWidgetId() {
    return widgetChartId;
  }

  public Widget getWidget() {
    return widget;
  }

  public UUID getResourceId() {
    return resourceId;
  }

  public String getMetricType() {
    return metricType;
  }
}