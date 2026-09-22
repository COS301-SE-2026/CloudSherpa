package com.cloudsherpa.service.alerts.dto;

import com.cloudsherpa.lib.entities.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record UpdateBudgetRequest(
    BigDecimal amount,
    CurrencyEnum currency,
    @JsonProperty("window_days") Integer windowDays,
    Boolean enabled) {}
