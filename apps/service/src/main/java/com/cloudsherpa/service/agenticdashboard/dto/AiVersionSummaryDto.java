package com.cloudsherpa.service.agenticdashboard.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiVersionSummaryDto(
    UUID versionId,
    Integer version,
    UUID parentVersionId,
    String title,
    OffsetDateTime createdAt,
    Boolean current) {}
