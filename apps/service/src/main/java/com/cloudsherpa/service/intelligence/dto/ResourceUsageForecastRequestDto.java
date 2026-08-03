package com.cloudsherpa.service.intelligence.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

// Assumptions made: the CloudSherpa generated UUID is the authoritative
// resource identifier to be used internally by CloudSherpa services
public record ResourceUsageForecastRequestDto(
    UUID resourceId, String metricType, OffsetDateTime forecastHorizon) {}
