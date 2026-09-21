package com.cloudsherpa.service.agenticdashboard.dto;

import com.cloudsherpa.lib.entities.PredefinedTimeEnum;
import java.time.OffsetDateTime;
import java.util.List;

public record DashboardPlanDto(
    String title,
    String description,
    OffsetDateTime timeFrom,
    OffsetDateTime timeTo,
    PredefinedTimeEnum predefinedTime,
    List<DashboardPlanWidgetDto> widgets) {}
