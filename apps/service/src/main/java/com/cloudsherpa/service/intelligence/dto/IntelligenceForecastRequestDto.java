package com.cloudsherpa.service.intelligence.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

// snake case used since it is what API contract expects
public record IntelligenceForecastRequestDto(
    int forecast_horizon, List<Instant> timestamps, List<BigDecimal> values, String model) {}
