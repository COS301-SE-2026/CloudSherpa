package com.cloudsherpa.lib.dtos;

import java.math.BigDecimal;
import java.time.Instant;

public record TimestampedNumericDataPoint(
    BigDecimal value,
    Instant timestamp
) {}
