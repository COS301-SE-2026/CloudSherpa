package com.cloudsherpa.lib.ingestion.cost.gcp;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.ingestion.cost.AbstractProviderIngestionCostCalculator;
import com.cloudsherpa.lib.ingestion.cost.IngestionWorkload;

public class GcpIngestionCostCalculator
    extends AbstractProviderIngestionCostCalculator {

  private final BigDecimal usdPerMillionTimeSeries;
  private final long freeTimeSeriesPerMonth;
  private final int minimumRecommendedPeriodSeconds;

  public GcpIngestionCostCalculator(
      BigDecimal usdPerMillionTimeSeries,
      long freeTimeSeriesPerMonth,
      int minimumRecommendedPeriodSeconds) {

    this.usdPerMillionTimeSeries = usdPerMillionTimeSeries;

    this.freeTimeSeriesPerMonth = freeTimeSeriesPerMonth;

    this.minimumRecommendedPeriodSeconds = minimumRecommendedPeriodSeconds;
  }

  @Override
  public ProviderEnum getProvider() {
    return ProviderEnum.GCP;
  }

  @Override
  public int getMinimumRecommendedPeriodSeconds() {
    return minimumRecommendedPeriodSeconds;
  }

  @Override
  protected BigDecimal calculateCostPerExecution(
      IngestionWorkload workload) {

    long billableSeries = Math.max(
        0,
        workload.totalMetricSeries());

    return BigDecimal.valueOf(billableSeries)
        .multiply(usdPerMillionTimeSeries)
        .divide(
            BigDecimal.valueOf(1_000_000),
            12,
            RoundingMode.HALF_UP);
  }
}
