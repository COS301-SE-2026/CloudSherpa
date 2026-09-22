package com.cloudsherpa.service.webhooks.dto;

import com.cloudsherpa.lib.entities.WebhookStatusEnum;
import java.util.List;
import java.util.UUID;

public record WebhookResponse(
    UUID webhookId,
    String webhookName,
    String endpointUrl,
    List<String> eventTypes,
    List<UUID> cloudAccounts,
    WebhookStatusEnum webhookStatus) {}
