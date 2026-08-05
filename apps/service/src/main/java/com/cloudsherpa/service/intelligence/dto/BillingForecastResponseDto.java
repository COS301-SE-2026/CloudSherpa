package com.cloudsherpa.service.intelligence.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record BillingForecastResponseDto(
    BigDecimal cumalativeBillingForecastValue,
    List<Instant> timestamps,
    Map<String, List<BigDecimal>> billingForecastSeries) {}
