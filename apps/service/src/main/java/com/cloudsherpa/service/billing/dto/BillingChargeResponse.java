package com.cloudsherpa.service.billing.dto;

import com.cloudsherpa.lib.entities.ProviderEnum;

public record BillingChargeResponse(
    String resourceId, String chargeId, String service, ProviderEnum provider) {}
