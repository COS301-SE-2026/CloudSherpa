package com.cloudsherpa.service.agenticdashboard.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiSessionResponseDto(
    UUID sessionId, OffsetDateTime createdAt, OffsetDateTime lastActivity, UUID currentVersionId) {}
