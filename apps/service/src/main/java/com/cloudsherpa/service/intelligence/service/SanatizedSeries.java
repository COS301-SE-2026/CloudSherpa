package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import java.util.List;

public record SanatizedSeries(
    List<TimestampedNumericDataPoint> timestampedNumericDataPoints, long periodicity) {}
