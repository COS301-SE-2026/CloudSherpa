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

  private NormalizedMetric(Builder builder) {
    this.metricId = builder.metricId;
    this.resourceId = builder.resourceId;
    this.accountId = builder.accountId;
    this.metricType = builder.metricType;
    this.metricName = builder.metricName;
    this.metricValue = builder.metricValue;
    this.unit = builder.unit;
    this.currency = builder.currency;
    this.periodStart = builder.periodStart;
    this.periodEnd = builder.periodEnd;
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

  public static class Builder {
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

    public Builder metricId(String metricId) {
      this.metricId = metricId;
      return this;
    }

    public Builder resourceId(String resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public Builder accountId(String accountId) {
      this.accountId = accountId;
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

    public Builder metricValue(double metricValue) {
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

    public Builder periodStart(long periodStart) {
      this.periodStart = periodStart;
      return this;
    }

    public Builder periodEnd(long periodEnd) {
      this.periodEnd = periodEnd;
      return this;
    }

    public NormalizedMetric build() {
      return new NormalizedMetric(this);
    }
  }
}
