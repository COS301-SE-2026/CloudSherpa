package com.cloudsherpa.lib.projections;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface AggregatedMetric 
{
    UUID getResourceId();
    String getMetricName();
    String getMetricType();
    BigDecimal getMetricValue();
    String getUnit();
    Instant getPeriodStart();
    Instant getPeriodEnd();
    Long getSampleCount();
}
