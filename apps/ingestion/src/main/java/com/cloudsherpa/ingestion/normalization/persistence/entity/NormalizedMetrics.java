package com.cloudsherpa.ingestion.normalization.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

  @Column(name = "recorded_at", nullable = false, updatable = false)
  private OffsetDateTime recordedAt;

  // This is the foreign key mapping to environment_reference table
  // Many-to-one because: One AWS environment will have multiple of individual metric records
  // associated with it.
  @ManyToOne(optional = false)
  @JoinColumn(name = "environment_id", nullable = false)
  private EnvironmentReference environmentReference;

  @Column(name = "resource_id", nullable = false, length = 255)
  private String resourceId;

  @Column(name = "service_category", nullable = false, length = 100)
  private String serviceCategory;

  @Column(name = "usage_unit", nullable = false, length = 50)
  private String usageUnit;

  @Column(name = "currency", length = 10, nullable = false)
  private String currency;

  // Use BigDecimal when working with financial data or measurement data
  // This is for better accuracy
  @Column(name = "usage_amount", nullable = false, precision = 19, scale = 6)
  private BigDecimal usageAmount;

  @Column(name = "cost_amount", nullable = false, precision = 19, scale = 6)
  private BigDecimal costAmount;

  public NormalizedMetrics() {
    // Default constructor
  }

  public NormalizedMetrics(
      OffsetDateTime recordedAt,
      EnvironmentReference environmentReference,
      String resourceId,
      String serviceCategory,
      BigDecimal usageAmount,
      String usageUnit,
      BigDecimal costAmount,
      String currency) {
    this.recordedAt = recordedAt;
    this.environmentReference = environmentReference;
    this.resourceId = resourceId;
    this.serviceCategory = serviceCategory;
    this.usageAmount = usageAmount;
    this.usageUnit = usageUnit;
    this.costAmount = costAmount;
    this.currency = currency;
  }

  public UUID getMetricId() {
    return metricId;
  }

  public OffsetDateTime getRecordedAt() {
    return recordedAt;
  }

  public EnvironmentReference getEnvironmentReference() {
    return environmentReference;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getServiceCategory() {
    return serviceCategory;
  }

  public String getUsageUnit() {
    return usageUnit;
  }

  public String getCurrency() {
    return currency;
  }

  public BigDecimal getUsageAmount() {
    return usageAmount;
  }

  public BigDecimal getCostAmount() {
    return costAmount;
  }
}
