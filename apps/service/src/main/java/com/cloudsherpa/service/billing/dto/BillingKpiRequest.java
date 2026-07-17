package com.cloudsherpa.service.billing.dto;

import java.util.List;
import java.util.UUID;

public record BillingKpiRequest(
    String title, List<UUID> resourceIds, String from, String to, String aggregation) {}
