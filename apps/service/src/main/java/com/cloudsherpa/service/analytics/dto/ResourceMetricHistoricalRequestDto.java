package com.cloudsherpa.service.analytics.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ResourceMetricHistoricalRequestDto(
    UUID resourceId, String metricType, OffsetDateTime fromDate) {}
