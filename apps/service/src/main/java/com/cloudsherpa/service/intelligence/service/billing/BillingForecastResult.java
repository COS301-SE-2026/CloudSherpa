package com.cloudsherpa.service.intelligence.service.billing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record BillingForecastResult(
    BigDecimal cumalativeForecastResult,
    Map<String, BigDecimal> individualChargeForecastResults,
    Map<String, List<BigDecimal>> individualChargeSeries,
    List<String> failedForecastCharges) {}
