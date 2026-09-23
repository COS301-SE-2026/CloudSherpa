package com.cloudsherpa.service.intelligence.dto;

import com.cloudsherpa.service.intelligence.model.ForecastSeries;
import java.math.BigDecimal;
import java.util.List;

public record BillingForecastResponseDto(
    BigDecimal cumalativeBillingForecastValue,
    BigDecimal cumalitivePastForecastingValue,
    ForecastSeries billingForecastSeries,
    List<String> failedForecastCharges,
    BigDecimal pastVariance,
    BigDecimal dailyBurnRate,
    String highestCostDriver,
    String highestCostAcceleration,
    BigDecimal accelerationRate) {}
