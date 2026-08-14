package com.cloudsherpa.service.intelligence.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record BillingForecastResponseDto(
    BigDecimal cumalativeBillingForecastValue,
    BigDecimal cumalitivePastForecastingValue,
    Map<String, BillingForecastValue> billingForecastSeries,
    List<String> failedForecastCharges,
    BigDecimal pastVariance,
    BigDecimal dailyBurnRate,
    String highestCostDriver) {}
