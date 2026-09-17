package com.cloudsherpa.service.webhooks.dto;

import com.cloudsherpa.lib.entities.WebhookStatusEnum;
import java.util.List;
import java.util.UUID;

public record EditWebhookDto(
    String name,
    String endpointUrl,
    List<String> eventTypes,
    WebhookStatusEnum status,
    List<UUID> cloudAccounts) {}
