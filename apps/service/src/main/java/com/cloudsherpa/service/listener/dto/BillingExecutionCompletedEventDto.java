package com.cloudsherpa.service.listener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record BillingExecutionCompletedEventDto(
    @JsonProperty("execution_id") UUID executionId,
    @JsonProperty("config_id") UUID configId,
    String status) {}
