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

  @Column(name = "widget_chart_id", nullable = false)
  private UUID widgetChartId;

  @ManyToOne
  @JoinColumn(name = "widget_chart_id", nullable = false, insertable = false, updatable = false)
  private WidgetChart widgetChart;

  @Column(name = "resource_id")
  private UUID resourceId;

  @Column(name = "metric_type", length = 50)
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

  public UUID getWidgetChartId() {
    return widgetChartId;
  }

  public void setWidgetChartId(UUID widgetChartId) {
    this.widgetChartId = widgetChartId;
  }

  public WidgetChart getWidgetChart() {
    return widgetChart;
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
