package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import com.cloudsherpa.lib.entities.ProviderEnum;
import java.util.UUID;

public record ResourceToolDto(
    UUID resourceId,
    UUID accountId,
    ProviderEnum provider,
    String resourceType,
    String resourceName,
    String resourceIdentifier,
    String region) {}
