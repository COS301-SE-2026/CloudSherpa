package com.cloudsherpa.service.billing.dto;

import java.util.UUID;

public record BillingResourceResponse(
    UUID resourceId,
    String resourceName,
    String service,
    String provider,
    UUID connectionId,
    String connectionName) {}
