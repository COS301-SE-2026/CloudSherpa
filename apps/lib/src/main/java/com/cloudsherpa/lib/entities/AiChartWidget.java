package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.MapsId;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_chart_widget", schema = "public")
public class AiChartWidget {

  @Id
  @Column(name = "widget_id")
  private UUID widgetId;

  @OneToOne
  @MapsId
  private AiDashboardWidget widget;

  @Column(name = "chart_type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ChartTypeEnum chartType;

  @Column(name = "chart_colour", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ChartColourEnum chartColour;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false)
  private ProviderEnum provider;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "resource_id", nullable = false)
  private UUID resourceId;

  @Column(name = "metric_type", nullable = false, length = 50)
  private String metricType;

  @Column(name = "metric_name", nullable = false, length = 100)
  private String metricName;

  protected AiChartWidget() {
  }

  private AiChartWidget(Builder builder) {
    this.widgetId = builder.widgetId;
    this.widget = builder.widget;
    this.chartType = builder.chartType;
    this.chartColour = builder.chartColour;
    this.provider = builder.provider;
    this.accountId = builder.accountId;
    this.resourceId = builder.resourceId;
    this.metricType = builder.metricType;
    this.metricName = builder.metricName;
  }

  public UUID getWidgetId() {
    return widgetId;
  }

  public AiDashboardWidget getWidget() {
    return widget;
  }

  public ChartTypeEnum getChartType() {
    return chartType;
  }

  public ChartColourEnum getChartColour() {
    return chartColour;
  }

  public ProviderEnum getProvider() {
    return provider;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public UUID getResourceId() {
    return resourceId;
  }

  public String getMetricType() {
    return metricType;
  }

  public String getMetricName() {
    return metricName;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID widgetId;
    private AiDashboardWidget widget;
    private ChartTypeEnum chartType;
    private ChartColourEnum chartColour;
    private ProviderEnum provider;
    private UUID accountId;
    private UUID resourceId;
    private String metricType;
    private String metricName;

    public Builder widgetId(UUID widgetId) {
      this.widgetId = widgetId;
      return this;
    }

    public Builder widget(AiDashboardWidget widget) {
      this.widget = widget;
      return this;
    }

    public Builder chartType(ChartTypeEnum chartType) {
      this.chartType = chartType;
      return this;
    }

    public Builder chartColour(ChartColourEnum chartColour) {
      this.chartColour = chartColour;
      return this;
    }

    public Builder provider(ProviderEnum provider) {
      this.provider = provider;
      return this;
    }

    public Builder accountId(UUID accountId) {
      this.accountId = accountId;
      return this;
    }

    public Builder resourceId(UUID resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public Builder metricType(String metricType) {
      this.metricType = metricType;
      return this;
    }

    public Builder metricName(String metricName) {
      this.metricName = metricName;
      return this;
    }

    public AiChartWidget build() {
      return new AiChartWidget(this);
    }
  }
}
