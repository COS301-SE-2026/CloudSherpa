package com.cloudsherpa.service.webhooks.dto;

import java.util.List;
import java.util.UUID;

public record AddWebhookDto(
    String name, String endpointUrl, List<String> eventTypes, List<UUID> cloudAccounts) {}
