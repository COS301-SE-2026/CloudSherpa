package com.cloudsherpa.ingestion.normalization.model;

// This creates a java object for each metric (corresponds to the fields in the database)
// These are the new fields that I think we would need for CloudSherpa

// I chose to keep it all in 1 table as I saw there would be a lot of joins between tables if we
// split the usage and billing
public class NormalizedMetric {
  private String metricId;
  private String resourceId;
  private String accountId;
  private String metricType;
  private String metricName;
  private double metricValue;
  private String unit;
  private String currency;
  private long periodStart;
  private long periodEnd;

  public NormalizedMetric(
      String metricId,
      String resourceId,
      String accountId,
      String metricType,
      String metricName,
      double metricValue,
      String unit,
      String currency,
      long periodStart,
      long periodEnd) {
    this.metricId = metricId;
    this.resourceId = resourceId;
    this.accountId = accountId;
    this.metricType = metricType;
    this.metricName = metricName;
    this.metricValue = metricValue;
    this.unit = unit;
    this.currency = currency;
    this.periodStart = periodStart;
    this.periodEnd = periodEnd;
  }

  public String getMetricId() {
    return metricId;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getAccountId() {
    return accountId;
  }

  public String getMetricType() {
    return metricType;
  }

  public String getMetricName() {
    return metricName;
  }

  public double getMetricValue() {
    return metricValue;
  }

  public String getUnit() {
    return unit;
  }

  public String getCurrency() {
    return currency;
  }

  public long getPeriodStart() {
    return periodStart;
  }

  public long getPeriodEnd() {
    return periodEnd;
  }
}
