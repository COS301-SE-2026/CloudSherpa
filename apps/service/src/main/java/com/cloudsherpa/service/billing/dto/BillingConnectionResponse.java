package com.cloudsherpa.service.billing.dto;

import java.util.UUID;

public record BillingConnectionResponse(
    UUID connectionId, String connectionName, String provider) {}
