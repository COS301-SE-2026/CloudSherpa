package com.cloudsherpa.service.webhooks.model;

import com.cloudsherpa.service.webhooks.dto.WebhookEventCloudAccount;
import com.fasterxml.jackson.databind.JsonNode;

public record DeliveryHttpBody(
    String type, long timestamp, WebhookEventCloudAccount account, JsonNode data) {}
