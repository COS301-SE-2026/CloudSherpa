package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "widget_chart", schema = "public")
public class WidgetChart {

  @Id
  @Column(name = "chart_id", nullable = false)
  private UUID id;

  @Column(name = "widget_id", nullable = false)
  private UUID widgetId;

  @ManyToOne
  @JoinColumn(name = "widget_id", nullable = false, insertable = false, updatable = false)
  private Widget widget;

  @Column(name = "chart_type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ChartTypeEnum chartType;

  protected WidgetChart() {}

  public WidgetChart(UUID id, UUID widgetId, ChartTypeEnum chartType) {
    this.id = id;
    this.widgetId = widgetId;
    this.chartType = chartType;
  }

  public UUID getId() {
    return id;
  }

  public UUID getWidgetId() {
    return widgetId;
  }

  public Widget getWidget() {
    return widget;
  }

  public ChartTypeEnum getChartType() {
    return chartType;
  }
}