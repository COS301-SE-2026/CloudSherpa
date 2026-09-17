package com.cloudsherpa.service.webhooks;

import com.cloudsherpa.lib.entities.ProviderEnum;
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
                  exampleId(definition.type()),
                  definition.type(),
                  Instant.ofEpochMilli(0),
                  new WebhookEventDto.WebhookEventCloudAccount("Example Account", ProviderEnum.AWS),
                  definition.examplePayload()));
    }
  }

  // Works on the assumption that event types in the format <parent>.<child> and that there is only
  // ever a
  // single delimter
  private String exampleId(String eventType) {
    String[] splitType = eventType.split("\\.");
    return "msg_demo_" + splitType[0] + "_" + splitType[1];
  }
}
