package com.cloudsherpa.service.webhooks.model;

public record DeliveryHeaders(String webhookId, String webhookTimestamp, String webhookSignature) {}
