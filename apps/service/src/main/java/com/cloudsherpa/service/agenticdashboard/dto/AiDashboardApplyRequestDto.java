package com.cloudsherpa.service.agenticdashboard.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AiDashboardApplyRequestDto(
    @NotNull AiDashboardApplyMode mode, UUID startedDashboardId) {}
