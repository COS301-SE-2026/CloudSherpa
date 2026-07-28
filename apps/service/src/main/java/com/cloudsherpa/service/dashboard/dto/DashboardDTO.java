package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.PredefinedTimeEnum;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DashboardDTO(
    UUID id,
    String displayName,
    OffsetDateTime timeFrom,
    OffsetDateTime timeTo,
    PredefinedTimeEnum predefinedTime,
    Boolean current,
    List<WidgetDTO> widgets) {}
