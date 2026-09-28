package com.cloudsherpa.lib.ingestion.cost;

import java.math.BigDecimal;

public record IngestionPeriodCalculation(
    int ingestionPeriodSeconds,
    BigDecimal estimatedMonthlyCost,
    boolean budgetSatisfied) {
}
