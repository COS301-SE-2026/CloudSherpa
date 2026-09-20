package com.cloudsherpa.service.alerts.dto;

import com.cloudsherpa.lib.entities.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateBudgetRequest(
    UUID userId,
    String scope,
    @JsonProperty("scope_id") UUID scopeId,
    BigDecimal amount,
    CurrencyEnum currency,
    @JsonProperty("window_days") Integer windowDays,
    Boolean enabled) {}
