package com.cloudsherpa.service.intelligence.dto;

import java.util.List;

// Request for forecasting a specific set of billing charges.
public record BillingForecastIndividualChargesRequestDto(
    List<String> chargeIds, Integer forecastSteps) {}
