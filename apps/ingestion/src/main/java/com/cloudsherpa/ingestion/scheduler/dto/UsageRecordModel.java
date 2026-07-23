package com.cloudsherpa.service.scheduler.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UsageRecordModel(
    UUID recordId,
    String provider,
    String accountId,
    String subscriptionId,
    String projectId,
    String resourceId,
    String resourceType,
    String serviceName,
    String region,
    String metricName,
    double value,
    String unit,
    Instant timestamp,
    Instant periodStart,
    Instant periodEnd,
    Map<String, String> dimensions,
    Map<String, String> tags,
    Instant ingestionTimestamp,
    String ingestionId,
    String source) {}
