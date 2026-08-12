package com.cloudsherpa.service.intelligence.dto;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import java.util.List;

public record SanitizedChargeSeries(
    List<TimestampedNumericDataPoint> timestampedNumericDataPoints,
    long periodicity,
    int forecastSteps) {}
