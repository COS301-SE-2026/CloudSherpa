package com.cloudsherpa.service.dashboard.dto;

import com.cloudsherpa.lib.entities.PredefinedTimeEnum;
import java.time.OffsetDateTime;

public record DashboardWindowUpdateDTO(
    PredefinedTimeEnum newTime, OffsetDateTime from, OffsetDateTime to) {}
