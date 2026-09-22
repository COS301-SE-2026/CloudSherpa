package com.cloudsherpa.service.webhooks;

import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.service.webhooks.dto.WebhookEventCloudAccount;
import com.cloudsherpa.service.webhooks.dto.WebhookEventDto;
import com.cloudsherpa.service.webhooks.events.WebhookEventDefinition;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventDefinitionRegistry {
  private final Map<String, WebhookEventDefinition<?>> definitions;
  private Map<String, Map<String, WebhookEventDto<?>>> eventDefinitionPayload;
  private final Environment environment;

  public WebhookEventDefinitionRegistry(
      Map<String, WebhookEventDefinition<?>> definitions, Environment environment) {
    this.definitions = definitions;
    this.environment = environment;
    eventDefinitionPayload = new HashMap<>();
    constructDefinitionPayload();
  }

  public Map<String, Map<String, WebhookEventDto<?>>> getEventDefinitions() {
    return eventDefinitionPayload;
  }

  private void constructDefinitionPayload() {
    for (WebhookEventDefinition<?> definition : definitions.values()) {

      // Exclude dev event from registered definitions when dev profile not active
      if (!environment.matchesProfiles("dev") && definition.type().equals("dev.event")) {
        continue;
      }

      eventDefinitionPayload
          .computeIfAbsent(definition.category(), k -> new HashMap<>())
          .put(
              definition.displayName(),
              new WebhookEventDto<>(
                  definition.type(),
                  Instant.ofEpochMilli(0),
                  new WebhookEventCloudAccount("Example Account", ProviderEnum.AWS),
                  definition.examplePayload()));
    }
  }
}
