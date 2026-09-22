package com.cloudsherpa.service.webhooks.dto;

import java.util.List;

public record PagedWebhookDeliveryResponse(
    List<WebhookDeliveryResponse> deliveries,
    int page,
    int size,
    long totalElements,
    int totalPages) {}
