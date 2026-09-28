package com.cloudsherpa.lib.ingestion.cost.aws;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.ingestion.cost.AbstractProviderIngestionCostCalculator;
import com.cloudsherpa.lib.ingestion.cost.IngestionWorkload;

public class AwsIngestionCostCalculator
    extends AbstractProviderIngestionCostCalculator {

  private final BigDecimal usdPerThousandMetrics;
  private final int minimumRecommendedPeriodSeconds;

  public AwsIngestionCostCalculator(
      BigDecimal usdPerThousandMetrics,
      int minimumRecommendedPeriodSeconds) {

    this.usdPerThousandMetrics = usdPerThousandMetrics;

    this.minimumRecommendedPeriodSeconds = minimumRecommendedPeriodSeconds;
  }

  @Override
  public ProviderEnum getProvider() {
    return ProviderEnum.AWS;
  }

  @Override
  public int getMinimumRecommendedPeriodSeconds() {
    return minimumRecommendedPeriodSeconds;
  }

  @Override
  protected BigDecimal calculateCostPerExecution(
      IngestionWorkload workload) {

    return BigDecimal.valueOf(
        workload.totalMetricSeries())
        .multiply(usdPerThousandMetrics)
        .divide(
            BigDecimal.valueOf(1000),
            12,
            RoundingMode.HALF_UP);
  }
}
