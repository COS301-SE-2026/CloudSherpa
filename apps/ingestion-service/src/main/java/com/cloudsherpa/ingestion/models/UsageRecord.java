package com.cloudsherpa.ingestion.models;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class UsageRecord {
  private UUID recordId;
  private String provider;

  private String accountId;
  private String subscriptionId;
  private String projectId;

  private String resourceId;
  private String resourceType;
  private String serviceName;
  private String region;

  private String metricName;
  private double value;
  private String unit;

  private Instant timestamp;
  private Instant periodStart;
  private Instant periodEnd;

  private Map<String, String> dimensions;

  private Map<String, String> tags;

  private Instant ingestionTimestamp;
  private String ingestionId;
  private String source;

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

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public Instant getPeriodStart() {
    return periodStart;
  }

  public void setPeriodStart(Instant periodStart) {
    this.periodStart = periodStart;
  }

  public Instant getPeriodEnd() {
    return periodEnd;
  }

  public void setPeriodEnd(Instant periodEnd) {
    this.periodEnd = periodEnd;
  }

  public Map<String, String> getDimensions() {
    return dimensions;
  }

  public void setDimensions(Map<String, String> dimensions) {
    this.dimensions = dimensions;
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
