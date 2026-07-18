package com.cloudsherpa.service.billing.dto;

import java.util.List;
import java.util.UUID;

public record BillingKpiConfigRequest(
    String title,
    UUID connectionId,
    List<UUID> resourceIds,
    String from,
    String to,
    String aggregation) {}
