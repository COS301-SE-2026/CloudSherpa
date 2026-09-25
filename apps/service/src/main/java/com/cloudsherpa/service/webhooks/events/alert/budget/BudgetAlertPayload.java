package com.cloudsherpa.service.webhooks.events.alert.budget;

import com.cloudsherpa.service.webhooks.events.WebhookPayload;
import java.math.BigDecimal;

public record BudgetAlertPayload(
    String title,
    String message,
    String budgetScope,
    String severity,
    BigDecimal budgetAmount,
    BigDecimal currentAmount,
    Integer windowDays)
    implements WebhookPayload {}
