package com.cloudsherpa.service.alerts.dto;

import com.cloudsherpa.lib.entities.Budget;
import com.cloudsherpa.lib.entities.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BudgetResponse(
    @JsonProperty("budget_id") UUID budgetId,
    @JsonProperty("user_id") UUID userId,
    String scope,
    @JsonProperty("scope_id") UUID scopeId,
    BigDecimal amount,
    CurrencyEnum currency,
    @JsonProperty("window_days") Integer windowDays,
    boolean enabled,
    @JsonProperty("created_at") OffsetDateTime createdAt,
    @JsonProperty("updated_at") OffsetDateTime updatedAt) {

  public static BudgetResponse from(Budget budget) {
    return new BudgetResponse(
        budget.getBudgetId(),
        budget.getUserId(),
        budget.getScope(),
        budget.getScopeId(),
        budget.getAmount(),
        budget.getCurrency(),
        budget.getWindowDays(),
        budget.isEnabled(),
        budget.getCreatedAt(),
        budget.getUpdatedAt());
  }
}
