package com.cloudsherpa.service.webhooks.model;

import com.cloudsherpa.service.webhooks.dto.WebhookEventCloudAccount;
import com.fasterxml.jackson.databind.JsonNode;

public record DeliveryHttpBody(
    String type, String timestamp, WebhookEventCloudAccount account, JsonNode data) {}
