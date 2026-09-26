package com.cloudsherpa.lib.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SegmentedMetric(
    long segmentId,
    UUID metricId,
    UUID resourceId,
    Instant recordedAt,
    String metricType,
    String metricName,
    BigDecimal metricValue,
    String unit,
    String currency,
    Instant periodStart,
    Instant periodEnd) {
}
