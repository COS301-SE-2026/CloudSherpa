package com.cloudsherpa.service.billing.dto;

import java.util.UUID;

public record BillingResourceResponse(
    String resourceId, String service, String provider, UUID connectionId, String connectionName) {}
