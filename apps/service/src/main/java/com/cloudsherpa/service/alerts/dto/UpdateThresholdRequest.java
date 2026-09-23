package com.cloudsherpa.service.alerts.dto;

import com.cloudsherpa.lib.entities.AlertSeverityEnum;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateThresholdRequest(
    @JsonProperty("metric_name") String metricName,
    String operator,
    Double value,
    AlertSeverityEnum severity,
    Boolean enabled) {}
