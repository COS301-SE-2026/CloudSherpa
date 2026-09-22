package com.cloudsherpa.service.alerts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateThresholdRequest(
    @JsonProperty("metric_name") String metricName,
    String operator,
    Double value,
    String severity,
    Boolean enabled) {}
