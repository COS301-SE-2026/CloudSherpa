package com.cloudsherpa.service.intelligence.service.billing;

import java.math.BigDecimal;

public record BillingForecastValue(
    BigDecimal value, BigDecimal percentageOfTotal, String chargeLabel) {}
