package com.cloudsherpa.lib.projections;

import com.cloudsherpa.lib.entities.ProviderEnum;
import java.math.BigDecimal;
import java.util.UUID;

public interface OptimizationStatisticsAggregate {

    UUID getResourceId();
    ProviderEnum getProvider();
    String getMetricName();
    BigDecimal getMinimumValue();
    BigDecimal getMaximumValue();
    BigDecimal getAverageValue();
    BigDecimal getMedianValue();
    BigDecimal getP95Value();
    BigDecimal getP99Value();
    BigDecimal getStandardDeviation();
}