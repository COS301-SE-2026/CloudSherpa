package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "widget_thresholds")
public class WidgetThreshold {

  @Id
  @Column(name = "threshold_id", nullable = false, updatable = false)
  private UUID thresholdId;

  @Column(name = "widget_id")
  private UUID widgetId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "metric_name", nullable = false)
  private String metricName;

  @Column(name = "operator", nullable = false)
  private String operator;

  @Column(name = "value", nullable = false)
  private double value;

  @Column(name = "severity")
  private String severity;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  public WidgetThreshold() {}

  public WidgetThreshold(
      UUID widgetId,
      UUID userId,
      String metricName,
      String operator,
      double value,
      String severity,
      boolean enabled) {
    this.thresholdId = UUID.randomUUID();
    this.widgetId = widgetId;
    this.userId = userId;
    this.metricName = metricName;
    this.operator = operator;
    this.value = value;
    this.severity = severity;
    this.enabled = enabled;
  }

  public UUID getThresholdId() {
    return thresholdId;
  }

  public void setThresholdId(UUID thresholdId) {
    this.thresholdId = thresholdId;
  }

  public UUID getWidgetId() {
    return widgetId;
  }

  public void setWidgetId(UUID widgetId) {
    this.widgetId = widgetId;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}