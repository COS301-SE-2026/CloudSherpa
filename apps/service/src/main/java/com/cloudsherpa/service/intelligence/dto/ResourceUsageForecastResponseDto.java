package com.cloudsherpa.service.intelligence.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Assumptions made:
// Chronos-2 quantile predictions made and quantiles want to be known, hence parallel arrays vs
// PredictedValue record
public record ResourceUsageForecastResponseDto(
    List<LocalDateTime> horizonTimestamps,
    List<BigDecimal> predictedValues,
    List<BigDecimal> q1Values,
    List<BigDecimal> q3Values) {}
