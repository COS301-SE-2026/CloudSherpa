package com.cloudsherpa.service.webhooks.events.threshold;

import com.cloudsherpa.service.webhooks.events.WebhookEventDefinition;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ThresholdAlertEventDefinition
    implements WebhookEventDefinition<ThresholdAlertPayload> {

  @Override
  public String type() {
    return "alert.threshold.triggered";
  }

  @Override
  public String category() {
    return "Alerts";
  }

  @Override
  public String displayName() {
    return "Usage Threshold Alert";
  }

  @Override
  public String description() {
    return "A resource metric crossed a configured threshold";
  }

  @Override
  public Class<ThresholdAlertPayload> payloadClass() {
    return ThresholdAlertPayload.class;
  }

  @Override
  public ThresholdAlertPayload examplePayload() {
    return new ThresholdAlertPayload(
        UUID.fromString("33333333-3333-3333-3333-333333333333"),
        "CPUUtilization",
        new BigDecimal("92.0"),
        "Percent",
        "GT",
        new BigDecimal("80.0"),
        "WARNING",
        OffsetDateTime.parse("2026-09-21T09:00:00Z"),
        OffsetDateTime.parse("2026-09-21T09:05:00Z"));
  }
}
