package com.cloudsherpa.service.webhooks.model;

import java.time.Instant;
import java.util.UUID;

public record WebhookDelivery(
    UUID deliveryId,
    Instant timestamp,
    UUID webhookId,
    String eventType,
    UUID cloudAccount,
    WebhookDeliveryResultEnum result,
    Integer responseCode) {}
