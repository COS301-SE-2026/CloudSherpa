package com.cloudsherpa.lib.projections;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface AggregatedMetric 
{
    UUID getResourceId();
    String getMetricName();
    String getMetricType();
    BigDecimal getMetricValue();
    String getUnit();
    OffsetDateTime getPeriodStart();
    OffsetDateTime getPeriodEnd();
    Long getSampleCount();
}
