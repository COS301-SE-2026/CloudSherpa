package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "threshold")
public class Threshold {

  @Id
  @Column(name = "threshold_id", nullable = false, updatable = false)
  private UUID thresholdId;

  @Column(name = "resource_id", nullable = false)
  private UUID resourceId;

  @ManyToOne
  @JoinColumn(name = "resource_id", nullable = false, insertable = false, updatable = false)
  private Resource resource;

  @Column(name = "user_id")
  private UUID userId;

  @ManyToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @Column(name = "metric_name", nullable = false)
  private String metricName;

  @Column(name = "operator", nullable = false)
  private String operator;

  @Column(name = "value", nullable = false)
  private double value;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "severity", columnDefinition = "public.alert_severity_enum")
  private AlertSeverityEnum severity;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  public Threshold() {}

  public Threshold(
      UUID resourceId,
      UUID userId,
      String metricName,
      String operator,
      double value,
      AlertSeverityEnum severity,
      boolean enabled) {
    this.thresholdId = UUID.randomUUID();
    this.resourceId = resourceId;
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

  public UUID getResourceId() {
    return resourceId;
  }

  public void setResourceId(UUID resourceId) {
    this.resourceId = resourceId;
  }

  public Resource getResource() {
    return resource;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public User getUser() {
    return user;
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

  public AlertSeverityEnum getSeverity() {
    return severity;
  }

  public void setSeverity(AlertSeverityEnum severity) {
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