package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import java.time.OffsetDateTime;

public record UpdateDashboardToolDto(
    String title,
    String description,
    OffsetDateTime timeFrom,
    OffsetDateTime timeTo,
    String predefinedTime) {}
