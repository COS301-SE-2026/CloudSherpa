package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import com.cloudsherpa.lib.entities.ProviderEnum;
import java.util.UUID;

public record CloudAccountToolDto(
    UUID accountId, String displayName, ProviderEnum provider, String accountType) {}
