package com.cloudsherpa.service.billing.dto;

import java.math.BigDecimal;

public record BillingKpiResponse(
    String title,
    BigDecimal value,
    String currency,
    int selectedResourceCount,
    String timeLabel,
    String updatedAt) {}
