package com.cloudsherpa.service.billing.dto;

import java.util.List;

public record BillingKpiRequest(
    String title, List<String> resourceIds, String from, String to, String aggregation) {}
