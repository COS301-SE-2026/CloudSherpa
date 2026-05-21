package com.cloudsherpa.service.analytics.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "normalized_metrics")
public class NormalizedMetrics {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "metric_id", nullable = false, updatable = false)
  private UUID metricId;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "resource_id", nullable = false, length = 255)
  private UUID resourceId;

  @Column(name = "recorded_at", updatable = false)
  private OffsetDateTime recordedAt;

  @Column(name = "metric_type", length = 50)
  private String metricType;

  @Column(name = "metric_name", length = 255)
  private String metricName;

  @Column(name = "metric_value", precision = 19, scale = 6)
  private BigDecimal metricValue;

  @Column(name = "unit", length = 50)
  private String unit;

  @Column(name = "currency", length = 10)
  private String currency;

  @Column(name = "period_start")
  private OffsetDateTime periodStart;

  @Column(name = "period_end")
  private OffsetDateTime periodEnd;

  public NormalizedMetrics() {
    // Default constructor
  }

  public NormalizedMetrics(
      UUID accountId,
      OffsetDateTime recordedAt,
      UUID resourceId,
      String metricType,
      String metricName,
      BigDecimal metricValue,
      String unit,
      String currency,
      OffsetDateTime periodStart,
      OffsetDateTime periodEnd) {
    this.accountId = accountId;
    this.resourceId = resourceId;
    this.recordedAt = recordedAt;
    this.metricType = metricType;
    this.metricName = metricName;
    this.metricValue = metricValue;
    this.unit = unit;
    this.currency = currency;
    this.periodStart = periodStart;
    this.periodEnd = periodEnd;
  }

  public UUID getMetricId() {
    return metricId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public UUID getResourceId() {
    return resourceId;
  }

  public OffsetDateTime getRecordedAt() {
    return recordedAt;
  }

  public String getMetricType() {
    return metricType;
  }

  public String getMetricName() {
    return metricName;
  }

  public BigDecimal getMetricValue() {
    return metricValue;
  }

  public String getUnit() {
    return unit;
  }

  public String getCurrency() {
    return currency;
  }

  public OffsetDateTime getPeriodStart() {
    return periodStart;
  }

  public OffsetDateTime getPeriodEnd() {
    return periodEnd;
  }
}
