package com.cloudsherpa.service.intelligence.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record IntelligenceForecastResponseDto(
    List<BigDecimal> forecast,
    List<Instant> timestamps,
    List<BigDecimal> q1,
    List<BigDecimal> q3) {}
