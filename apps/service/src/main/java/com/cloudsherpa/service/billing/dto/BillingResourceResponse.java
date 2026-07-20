package com.cloudsherpa.service.billing.dto;

import com.cloudsherpa.lib.entities.ProviderEnum;

public record BillingResourceResponse(String resourceId, String service, ProviderEnum provider) {}
