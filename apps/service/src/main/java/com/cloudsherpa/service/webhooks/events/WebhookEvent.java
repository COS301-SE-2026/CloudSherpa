package com.cloudsherpa.service.webhooks.events;

import java.time.Instant;
import java.util.UUID;

// Shared envelope for the actual (not metadata) event
public record WebhookEvent<T extends WebhookPayload>(
    UUID userId, String id, String type, Instant timestamp, UUID cloudAccountId, T data) {}
