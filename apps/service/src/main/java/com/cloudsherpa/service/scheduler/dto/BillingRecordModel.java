package com.cloudsherpa.service.scheduler.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record BillingRecordModel(
    UUID recordId,
    String provider,
    String accountId,
    String subscriptionId,
    String projectId,
    String billingAccountId,
    String serviceName,
    String resourceId,
    String resourceType,
    String region,
    double cost,
    double usageQuantity,
    String unit,
    String currency,
    String pricingModel,
    Instant usageStartTime,
    Instant usageEndTime,
    Instant billingPeriodStart,
    Instant billingPeriodEnd,
    Map<String, String> tags,
    Instant ingestionTimestamp,
    String ingestionId,
    String source) {}
