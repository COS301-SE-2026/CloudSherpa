package com.cloudsherpa.service.intelligence.service.billing;

import java.math.BigDecimal;
import java.util.Map;

public record BillingAnalyticsResult(
    BigDecimal cumalitivePastForecastValue,
    Map<String, BillingForecastValue> billingForecastSeries,
    BigDecimal pastVariance,
    BigDecimal dailyBurnRate,
    String highestCostDriver,
    String highestCostAcceleration) {}
