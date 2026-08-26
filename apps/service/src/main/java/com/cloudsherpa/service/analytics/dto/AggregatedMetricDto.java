package com.cloudsherpa.service.analytics.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AggregatedMetricDto(
    UUID resourceId,
    String metricType,
    String metricName,
    BigDecimal metricValue,
    String unit,
    OffsetDateTime periodStart,
    OffsetDateTime periodEnd,
    Long sampleCount) {}
