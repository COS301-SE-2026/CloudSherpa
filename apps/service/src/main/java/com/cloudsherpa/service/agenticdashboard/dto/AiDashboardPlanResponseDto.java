package com.cloudsherpa.service.agenticdashboard.dto;

import java.util.UUID;

public record AiDashboardPlanResponseDto(
    UUID sessionId,
    UUID versionId,
    Integer version,
    String assistantMessage,
    DashboardPlanDto dashboard) {}
