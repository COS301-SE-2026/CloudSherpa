package com.cloudsherpa.service.webhooks.events.alert.anomaly;

import com.cloudsherpa.service.webhooks.events.WebhookEventDefinition;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class AnomalyAlertDefinition implements WebhookEventDefinition<AnomalyAlertPayload> {
  @Override
  public String type() {
    return "alert.anomaly";
  }

  @Override
  public String category() {
    return "Alerts";
  }

  @Override
  public String displayName() {
    return "Usage Anoomaly Alert";
  }

  @Override
  public String description() {
    return "An anomaly was detected for a resource metric";
  }

  @Override
  public Class<AnomalyAlertPayload> payloadClass() {
    return AnomalyAlertPayload.class;
  }

  @Override
  public AnomalyAlertPayload examplePayload() {
    return new AnomalyAlertPayload(
        "i-0abcdef1234567890",
        "Production",
        "CPUUtilization",
        new BigDecimal("92.0"),
        new BigDecimal("60.0"),
        3.2,
        "CRITICAL",
        OffsetDateTime.parse("2026-09-21T09:00:00Z"),
        OffsetDateTime.parse("2026-09-21T09:05:00Z"));
  }
}
