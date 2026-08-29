package com.cloudsherpa.lib.dtos;

import java.util.UUID;

public record ResourceMetricEntry(
    UUID resourceId,
    String metricType,
    String metricName 
) {}
