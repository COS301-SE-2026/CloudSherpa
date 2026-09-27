package com.cloudsherpa.service.billing.dto;

import com.cloudsherpa.lib.entities.ProviderEnum;
import io.micrometer.common.lang.Nullable;
import java.math.BigDecimal;
import java.util.Map;

public record BillingChargeResponse(
    String resourceId,
    String chargeId,
    String service,
    ProviderEnum provider,
    @Nullable String resourceName,
    BigDecimal chargeCost,
    Map<String, Object> metadata) {}
