package com.cloudsherpa.service.webhooks;

import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.service.webhooks.dto.WebhookEventCloudAccount;
import com.cloudsherpa.service.webhooks.dto.WebhookEventDto;
import com.cloudsherpa.service.webhooks.events.WebhookEventDefinition;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventDefinitionRegistry {
  private final Map<String, WebhookEventDefinition<?>> definitions;
  private Map<String, Map<String, WebhookEventDto<?>>> eventDefinitionPayload;

  public WebhookEventDefinitionRegistry(Map<String, WebhookEventDefinition<?>> definitions) {
    this.definitions = definitions;
    eventDefinitionPayload = new HashMap<>();
    constructDefinitionPayload();
  }

  public Map<String, Map<String, WebhookEventDto<?>>> getEventDefinitions() {
    return eventDefinitionPayload;
  }

  private void constructDefinitionPayload() {
    for (WebhookEventDefinition<?> definition : definitions.values()) {
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
