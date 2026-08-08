package com.cloudsherpa.service.intelligence.dto;

import java.math.BigDecimal;
import java.util.Map;

public record BillingForecastResponseDto(
    BigDecimal cumalativeBillingForecastValue, Map<String, BigDecimal> billingForecastSeries) {}
