package com.cloudsherpa.service.billing.dto;

import java.math.BigDecimal;

public record BillingKpiResponse(
    BigDecimal value,
    String currency,
    int selectedChargeCount,
    String timeLabel,
    String updatedAt,
    BigDecimal previousValue) {}
