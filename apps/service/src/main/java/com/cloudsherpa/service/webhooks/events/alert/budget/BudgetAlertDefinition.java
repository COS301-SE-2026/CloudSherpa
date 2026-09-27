package com.cloudsherpa.service.webhooks.events.alert.budget;

import com.cloudsherpa.service.webhooks.events.WebhookEventDefinition;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class BudgetAlertDefinition implements WebhookEventDefinition<BudgetAlertPayload> {
  @Override
  public String type() {
    return "alert.budget";
  }

  @Override
  public String category() {
    return "Alerts";
  }

  @Override
  public String displayName() {
    return "Billing Budget Alert";
  }

  @Override
  public String description() {
    return "The current spend has crossed the set budget threshold.";
  }

  @Override
  public Class<BudgetAlertPayload> payloadClass() {
    return BudgetAlertPayload.class;
  }

  @Override
  public BudgetAlertPayload examplePayload() {
    return new BudgetAlertPayload(
        "Current spend exceeded budget of: 1000.00",
        "Current spend 1250.50 has reached budget amount 1000.00",
        "RESOURCE",
        "WARNING",
        new BigDecimal("1000.00"),
        new BigDecimal("1250.50"),
        30);
  }
}
