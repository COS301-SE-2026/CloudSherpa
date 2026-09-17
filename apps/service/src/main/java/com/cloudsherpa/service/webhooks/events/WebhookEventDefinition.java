package com.cloudsherpa.service.webhooks.events;

public interface WebhookEventDefinition<T extends WebhookPayload> {
  String type();

  String category();

  String displayName();

  String description();

  T examplePayload();
}
