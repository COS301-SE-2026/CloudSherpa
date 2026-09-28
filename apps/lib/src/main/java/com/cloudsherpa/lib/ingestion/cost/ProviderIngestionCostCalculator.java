package com.cloudsherpa.lib.ingestion.cost;

import com.cloudsherpa.lib.entities.ProviderEnum;
import java.math.BigDecimal;

public interface ProviderIngestionCostCalculator {

  ProviderEnum getProvider();

  int getMinimumRecommendedPeriodSeconds();

  BigDecimal calculateMonthlyCost(
      IngestionWorkload workload,
      int ingestionPeriodSeconds);

  IngestionPeriodCalculation calculatePeriodForBudget(
      IngestionWorkload workload,
      BigDecimal budgetDollars);
}
