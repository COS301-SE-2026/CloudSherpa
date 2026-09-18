package com.cloudsherpa.service.webhooks.dto;

import com.cloudsherpa.lib.entities.WebhookDeliveryStatusEnum;
import java.time.Instant;
import java.util.UUID;

public record WebhookDeliveryResponse(
    UUID deliveryId,
    Instant timestamp,
    UUID webhookId,
    String eventType,
    UUID cloudAccount,
    WebhookDeliveryStatusEnum result,
    Integer responseCode) {}
