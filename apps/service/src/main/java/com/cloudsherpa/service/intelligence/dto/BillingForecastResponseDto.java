package com.cloudsherpa.service.intelligence.dto;

import com.cloudsherpa.service.intelligence.service.billing.BillingForecastValue;
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
