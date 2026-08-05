package com.cloudsherpa.service.intelligence.dto;

import java.time.OffsetDateTime;
import java.util.List;

// Assumptions:
// As far as I am aware we use a mix of Instant and OffsetDatetime for our Date values
// using offsetdatetime here to preserve UTC offset from client calendar
public record BillingForecastRequestDto(OffsetDateTime forecastHorizon, List<String> chargeIds) {}
