package com.cloudsherpa.service.agenticdashboard.mcp.dto;

import com.cloudsherpa.lib.entities.ProviderEnum;

public record BillingChargeToolDto(
    String resourceId, String chargeId, String service, ProviderEnum provider) {}
