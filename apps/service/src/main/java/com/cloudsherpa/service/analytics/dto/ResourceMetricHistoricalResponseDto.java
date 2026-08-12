package com.cloudsherpa.service.analytics.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ResourceMetricHistoricalResponseDto(
    List<BigDecimal> values, List<OffsetDateTime> timestamps) {}
