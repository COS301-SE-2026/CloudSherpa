package com.cloudsherpa.service.analytics.dto;

import java.time.Instant;
import java.util.UUID;

public record DownsampledSeriesRequestDto(
    UUID resourceId, String metricName, Instant from, Instant to) {}
