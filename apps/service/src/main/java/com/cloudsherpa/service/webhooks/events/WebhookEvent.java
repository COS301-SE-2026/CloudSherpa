package com.cloudsherpa.service.webhooks.events;

import com.cloudsherpa.lib.entities.ProviderEnum;
import java.time.Instant;

public record WebhookEvent<T extends WebhookPayload>(
    String id, String type, Instant timestamp, WebhookEventCloudAccount account, T data) {
  public record WebhookEventCloudAccount(String name, ProviderEnum provider) {}
}
