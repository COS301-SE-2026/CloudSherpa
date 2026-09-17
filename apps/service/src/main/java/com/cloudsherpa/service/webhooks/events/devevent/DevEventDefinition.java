package com.cloudsherpa.service.webhooks.events.devevent;

import com.cloudsherpa.service.webhooks.events.WebhookEventDefinition;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Component;

// Definition for dev.event for use during dev and illustration of event definition
@Component
public class DevEventDefinition implements WebhookEventDefinition<DevPayload> {
  @Override
  public String type() {
    return "dev.event";
  }

  @Override
  public String category() {
    return "Dev";
  }

  @Override
  public String displayName() {
    return "Dev Event";
  }

  @Override
  public String description() {
    return "Development event used for internal testing";
  }

  @Override
  public DevPayload examplePayload() {
    return new DevPayload("Dev Event String", new BigDecimal("20.0"), Instant.ofEpochMilli(0));
  }
}
