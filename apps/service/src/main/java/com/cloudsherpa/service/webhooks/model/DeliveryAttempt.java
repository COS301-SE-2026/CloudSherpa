package com.cloudsherpa.service.webhooks.model;

import com.cloudsherpa.service.webhooks.dto.WebhookEventCloudAccount;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record DeliveryAttempt(
    DeliveryHeaders headers,
    String webhookUrl,
    String type,
    Instant timestamp,
    WebhookEventCloudAccount account,
    JsonNode data,
    String signingKey,
    Integer attemptCount) {}
