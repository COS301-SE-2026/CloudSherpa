package com.cloudsherpa.service.webhooks.model;

import java.util.List;
import java.util.UUID;

public record Webhook(
    String name,
    String endpointUrl,
    List<String> eventTypes,
    WebhookStatusEnum status,
    // If null webhook for all accountss
    List<UUID> cloudAccounts) {}
