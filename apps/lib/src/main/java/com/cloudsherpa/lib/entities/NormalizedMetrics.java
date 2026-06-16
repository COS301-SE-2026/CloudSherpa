package com.cloudsherpa.lib.entities;

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

  private NormalizedMetrics(Builder builder) {
    this.accountId = builder.accountId;
    this.resourceId = builder.resourceId;
    this.recordedAt = builder.recordedAt;
    this.metricType = builder.metricType;
    this.metricName = builder.metricName;
    this.metricValue = builder.metricValue;
    this.unit = builder.unit;
    this.currency = builder.currency;
    this.periodStart = builder.periodStart;
    this.periodEnd = builder.periodEnd;
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

  public static class Builder {
    private UUID accountId;
    private UUID resourceId;
    private OffsetDateTime recordedAt;
    private String metricType;
    private String metricName;
    private BigDecimal metricValue;
    private String unit;
    private String currency;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;

    public Builder accountId(UUID accountId) {
      this.accountId = accountId;
      return this;
    }

    public Builder resourceId(UUID resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public Builder recordedAt(OffsetDateTime recordedAt) {
      this.recordedAt = recordedAt;
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

    public Builder metricValue(BigDecimal metricValue) {
      this.metricValue = metricValue;
      return this;
    }

    public Builder unit(String unit) {
      this.unit = unit;
      return this;
    }

    public Builder currency(String currency) {
      this.currency = currency;
      return this;
    }

    public Builder periodStart(OffsetDateTime periodStart) {
      this.periodStart = periodStart;
      return this;
    }

    public Builder periodEnd(OffsetDateTime periodEnd) {
      this.periodEnd = periodEnd;
      return this;
    }

    public NormalizedMetrics build() {
      return new NormalizedMetrics(this);
    }
  }
}
