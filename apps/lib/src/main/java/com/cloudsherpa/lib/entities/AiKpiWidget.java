package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.MapsId;

import java.util.UUID;

@Entity
@Table(name = "ai_kpi_widget", schema = "public")
public class AiKpiWidget {

  @Id
  @Column(name = "widget_id")
  private UUID widgetId;

  @OneToOne
  @MapsId
  @JoinColumn(name = "widget_id", referencedColumnName = "widget_id")

  private AiDashboardWidget widget;
  @Column(name = "charge_ids", nullable = false, columnDefinition = "varchar(2128)[]")
  private String[] chargeIds;

  @Column(name = "aggregation_window_days", nullable = false)
  private Integer aggregationWindowDays;

  protected AiKpiWidget() {
  }

  private AiKpiWidget(Builder builder) {
    this.widgetId = builder.widgetId;
    this.widget = builder.widget;
    this.chargeIds = builder.chargeIds;
    this.aggregationWindowDays = builder.aggregationWindowDays;
  }

  public UUID getWidgetId() {
    return widgetId;
  }

  public AiDashboardWidget getWidget() {
    return widget;
  }

  public String[] getChargeIds() {
    return chargeIds;
  }

  public Integer getAggregationWindowDays() {
    return aggregationWindowDays;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID widgetId;
    private AiDashboardWidget widget;
    private String[] chargeIds;
    private Integer aggregationWindowDays;

    public Builder widgetId(UUID widgetId) {
      this.widgetId = widgetId;
      return this;
    }

    public Builder widget(AiDashboardWidget widget) {
      this.widget = widget;
      return this;
    }

    public Builder chargeIds(String[] chargeIds) {
      this.chargeIds = chargeIds;
      return this;
    }

    public Builder aggregationWindowDays(Integer aggregationWindowDays) {
      this.aggregationWindowDays = aggregationWindowDays;
      return this;
    }

    public AiKpiWidget build() {
      return new AiKpiWidget(this);
    }
  }
}
