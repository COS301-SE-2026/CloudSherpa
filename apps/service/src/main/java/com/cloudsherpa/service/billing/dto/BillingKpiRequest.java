package com.cloudsherpa.service.billing.dto;

import java.util.List;

public record BillingKpiRequest(
    List<String> chargeIds, String from, String to, String aggregation) {}
