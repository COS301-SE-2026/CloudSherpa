package com.cloudsherpa.service.webhooks.events.alert.threshold;

import com.cloudsherpa.lib.entities.AlertSeverityEnum;
import com.cloudsherpa.service.webhooks.events.WebhookEventDefinition;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class ThresholdAlertEventDefinition
    implements WebhookEventDefinition<ThresholdAlertPayload> {

  @Override
  public String type() {
    return "alert.threshold";
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
        "i-0abcdef1234567890",
        "Production",
        "CPUUtilization",
        new BigDecimal("92.0"),
        "Percent",
        "GT",
        80.0,
        AlertSeverityEnum.WARNING,
        OffsetDateTime.parse("2026-09-21T09:00:00Z"),
        OffsetDateTime.parse("2026-09-21T09:05:00Z"));
  }
}
