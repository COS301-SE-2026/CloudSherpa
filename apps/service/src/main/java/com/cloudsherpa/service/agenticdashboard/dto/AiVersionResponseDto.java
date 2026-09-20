package com.cloudsherpa.service.agenticdashboard.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiVersionResponseDto(
    UUID versionId,
    UUID sessionId,
    Integer version,
    UUID parentVersionId,
    Boolean current,
    OffsetDateTime createdAt,
    DashboardPlanDto dashboard) {}
