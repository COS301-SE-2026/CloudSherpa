package com.cloudsherpa.service.intelligence.dto;

import java.time.Instant;
import java.util.List;

// Assumptions made:
// Chronos-2 quantile predictions made and quantiles want to be known, hence parallel arrays vs
// PredictedValue record
public record ResourceUsageForecastResponseDto(
    List<Instant> horizonTimestamps,
    List<Double> predictedValues,
    List<Double> q1Values,
    List<Double> q3Values) {}
