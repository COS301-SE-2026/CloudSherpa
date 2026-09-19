package com.cloudsherpa.service.webhooks.exceptions;

import java.util.UUID;

public class WebhookNotFoundException extends IllegalStateException {
  public WebhookNotFoundException(UUID webhookId) {
    super("Webhook with ID " + webhookId.toString() + " not found");
  }
}
