package com.cloudsherpa.service.webhooks.dto;

import com.cloudsherpa.service.webhooks.events.WebhookPayload;
import java.time.Instant;

public record WebhookEventDto<T extends WebhookPayload>(
    String id, String type, Instant timestamp, WebhookEventCloudAccount account, T data) {}
