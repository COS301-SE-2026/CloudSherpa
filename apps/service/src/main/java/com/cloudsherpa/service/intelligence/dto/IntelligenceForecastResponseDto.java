package com.cloudsherpa.service.intelligence.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record IntelligenceForecastResponseDto(
    List<BigDecimal> forecast,
    List<LocalDateTime> timestamps,
    List<BigDecimal> q1,
    List<BigDecimal> q3) {}
