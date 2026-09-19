package com.cloudsherpa.service.webhooks.dto;

import com.cloudsherpa.lib.entities.ProviderEnum;

public record WebhookEventCloudAccount(String name, ProviderEnum provider) {}
