package com.cloudsherpa.ingestion.models;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class BillingRecord {
  private UUID recordId;
  private String provider;

  private String accountId; // AWS
  private String subscriptionId; // Azure
  private String projectId; // GCP
  private String billingAccountId; // Azure/GCP

  private String serviceName; // EC2, VM, BigQuery
  private String resourceId;
  private String resourceType;
  private String region;

  private double cost;
  private double usageQuantity;
  private String unit;

  private String currency;
  private String pricingModel; // OnDemand, Reserved, Spot

  private Instant usageStartTime;
  private Instant usageEndTime;
  private Instant billingPeriodStart;
  private Instant billingPeriodEnd;

  private Map<String, String> tags;

  private Instant ingestionTimestamp;
  private String ingestionId; // trace batch/job
  private String source; // CUR, API, Export

  public UUID getRecordId() {
    return recordId;
  }

  public void setRecordId(UUID recordId) {
    this.recordId = recordId;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getBillingAccountId() {
    return billingAccountId;
  }

  public void setBillingAccountId(String billingAccountId) {
    this.billingAccountId = billingAccountId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public double getCost() {
    return cost;
  }

  public void setCost(double cost) {
    this.cost = cost;
  }

  public double getUsageQuantity() {
    return usageQuantity;
  }

  public void setUsageQuantity(double usageQuantity) {
    this.usageQuantity = usageQuantity;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getPricingModel() {
    return pricingModel;
  }

  public void setPricingModel(String pricingModel) {
    this.pricingModel = pricingModel;
  }

  public Instant getUsageStartTime() {
    return usageStartTime;
  }

  public void setUsageStartTime(Instant usageStartTime) {
    this.usageStartTime = usageStartTime;
  }

  public Instant getUsageEndTime() {
    return usageEndTime;
  }

  public void setUsageEndTime(Instant usageEndTime) {
    this.usageEndTime = usageEndTime;
  }

  public Instant getBillingPeriodStart() {
    return billingPeriodStart;
  }

  public void setBillingPeriodStart(Instant billingPeriodStart) {
    this.billingPeriodStart = billingPeriodStart;
  }

  public Instant getBillingPeriodEnd() {
    return billingPeriodEnd;
  }

  public void setBillingPeriodEnd(Instant billingPeriodEnd) {
    this.billingPeriodEnd = billingPeriodEnd;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }

  public Instant getIngestionTimestamp() {
    return ingestionTimestamp;
  }

  public void setIngestionTimestamp(Instant ingestionTimestamp) {
    this.ingestionTimestamp = ingestionTimestamp;
  }

  public String getIngestionId() {
    return ingestionId;
  }

  public void setIngestionId(String ingestionId) {
    this.ingestionId = ingestionId;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}
