package com.cloudsherpa.service.listener.dto;

import java.util.UUID;

public record BillingExecutionCompletedEventDto(UUID executionId, UUID configId, String status) {}
