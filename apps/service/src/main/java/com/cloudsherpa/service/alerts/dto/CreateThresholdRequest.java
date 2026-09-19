package com.cloudsherpa.service.alerts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record CreateThresholdRequest(
    UUID resourceId,
    UUID userId,
    @JsonProperty("metric_name") String metricName,
    String operator,
    Double value,
    String severity,
    Boolean enabled) {}
