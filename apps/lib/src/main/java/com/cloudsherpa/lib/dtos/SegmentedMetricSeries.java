package com.cloudsherpa.lib.dtos;

import java.time.Instant;

public record SegmentedMetricSeries(long segmentId, Instant ts, double value) {
} 
