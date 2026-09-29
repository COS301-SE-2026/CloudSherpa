package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import com.cloudsherpa.lib.entities.ProviderEnum;

public record MetricToolDto(
    ProviderEnum provider,
    String serviceType,
    String metricName,
    String identifierField,
    String expectedUnit,
    String description) {}
