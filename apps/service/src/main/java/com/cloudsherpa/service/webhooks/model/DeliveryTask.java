package com.cloudsherpa.service.webhooks.model;

import java.util.UUID;

public record DeliveryTask(UUID tenantId, UUID deliveryId) {}
