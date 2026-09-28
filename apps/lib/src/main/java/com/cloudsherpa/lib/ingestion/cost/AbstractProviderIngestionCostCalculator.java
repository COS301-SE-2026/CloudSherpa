package com.cloudsherpa.lib.ingestion.cost;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class AbstractProviderIngestionCostCalculator
    implements ProviderIngestionCostCalculator {

  protected static final int MONTHLY_HOURS = 750;

  @Override
  public BigDecimal calculateMonthlyCost(
      IngestionWorkload workload,
      int ingestionPeriodSeconds) {

    validatePeriod(ingestionPeriodSeconds);

    BigDecimal costPerExecution = calculateCostPerExecution(workload);

    BigDecimal executionsPerMonth = BigDecimal.valueOf(MONTHLY_HOURS * 3600L)
        .divide(
            BigDecimal.valueOf(ingestionPeriodSeconds),
            12,
            RoundingMode.HALF_UP);

    return costPerExecution.multiply(
        executionsPerMonth);
  }

  @Override
  public IngestionPeriodCalculation calculatePeriodForBudget(
      IngestionWorkload workload,
      BigDecimal budgetDollars) {

    validateBudget(budgetDollars);

    int minimumPeriod = getMinimumRecommendedPeriodSeconds();

    if (budgetDollars.signum() == 0) {
      BigDecimal minimumCost = calculateMonthlyCost(
          workload,
          minimumPeriod);

      return new IngestionPeriodCalculation(
          minimumPeriod,
          minimumCost,
          minimumCost.signum() == 0);
    }

    BigDecimal costPerExecution = calculateCostPerExecution(workload);

    BigDecimal requiredPeriod = costPerExecution
        .multiply(
            BigDecimal.valueOf(MONTHLY_HOURS * 3600L))
        .divide(
            budgetDollars,
            0,
            RoundingMode.CEILING);

    int actualPeriod = Math.max(
        requiredPeriod.intValueExact(),
        minimumPeriod);

    BigDecimal actualCost = calculateMonthlyCost(
        workload,
        actualPeriod);

    return new IngestionPeriodCalculation(
        actualPeriod,
        actualCost,
        actualCost.compareTo(budgetDollars) <= 0);
  }

  protected abstract BigDecimal calculateCostPerExecution(
      IngestionWorkload workload);

  protected void validatePeriod(int seconds) {
    if (seconds <= 0) {
      throw new IllegalArgumentException(
          "Ingestion period must be greater than zero");
    }
  }

  protected void validateBudget(BigDecimal budget) {
    if (budget == null || budget.signum() < 0) {
      throw new IllegalArgumentException(
          "Ingestion budget must be zero or greater");
    }
  }
}
