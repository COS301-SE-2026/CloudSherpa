package com.cloudsherpa.ingestion.normalization.model;

// This creates a java object for each metric (corresponds to the fields in the database)
// These are the new fields that I think we would need for CloudSherpa

// I chose to keep it all in 1 table as I saw there would be a lot of joins between tables if we
// split the usage and billing
public class NormalizedMetric {
  private String metricId;
  private String provider;
  private long usageStart;
  private long usageEnd;
  private String resourceId;
  private String service;
  private String serviceCategory;
  private double usageAmount;
  private String usageUnit;
  private double effectiveCost;
  private String currency;
  private String pricingModel;

  public NormalizedMetric(
      String metricId,
      String provider,
      long usageStart,
      long usageEnd,
      String resourceId,
      String service,
      String serviceCategory,
      double usageAmount,
      String usageUnit,
      double effectiveCost,
      String currency,
      String pricingModel) {
    this.metricId = metricId;
    this.provider = provider;
    this.usageStart = usageStart;
    this.usageEnd = usageEnd;
    this.resourceId = resourceId;
    this.service = service;
    this.serviceCategory = serviceCategory;
    this.usageAmount = usageAmount;
    this.usageUnit = usageUnit;
    this.effectiveCost = effectiveCost;
    this.currency = currency;
    this.pricingModel = pricingModel;
  }

  public String getMetricId() {
    return metricId;
  }

  public String getProvider() {
    return provider;
  }

  public long getUsageStart() {
    return usageStart;
  }

  public long getUsageEnd() {
    return usageEnd;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getService() {
    return service;
  }

  public String getServiceCategory() {
    return serviceCategory;
  }

  public double getUsageAmount() {
    return usageAmount;
  }

  public String getUsageUnit() {
    return usageUnit;
  }

  public double getEffectiveCost() {
    return effectiveCost;
  }

  public String getCurrency() {
    return currency;
  }

  public String getPricingModel() {
    return pricingModel;
  }
}
