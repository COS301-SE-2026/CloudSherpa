package com.cloudsherpa.service.agenticdashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AiDashboardPlanRequestDto(@NotNull UUID sessionId, @NotBlank String message) {}
