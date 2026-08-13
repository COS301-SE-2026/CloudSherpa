package com.cloudsherpa.service.intelligence.dto;

import java.math.BigDecimal;

public record BillingForecastValue(BigDecimal value, BigDecimal percentageOfTotal, String chargeLabel) {}
