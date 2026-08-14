package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BillingAnalyticsService {

  private final Logger logger = LoggerFactory.getLogger(BillingAnalyticsService.class);

  private final NormalizedCostsRepository normalizedCostsRepository;

  private static final Integer DAY_IN_SECONDS = 86400;

  public BillingAnalyticsService(NormalizedCostsRepository normalizedCostsRepository) {
    this.normalizedCostsRepository = normalizedCostsRepository;
  }

  public BillingAnalyticsResult process(
      BillingForecastResult billingForecastResult, Integer historicalDays) {

    BigDecimal cumalativeHistorical =
        getCumalativePast(billingForecastResult.firstForecastTimestamp(), historicalDays);
    BigDecimal pastVariance =
        variance(cumalativeHistorical, billingForecastResult.cumalativeForecastResult());
    String highestCostDriver =
        primaryCostDriver(billingForecastResult.individualChargeForecastResults());
    String highestCostAcceleration =
        highestCostAcceleration(billingForecastResult.individualChargeSeries());

    Map<String, BillingForecastValue> billingForecastSeries =
        buildBillingForecastSeries(
            billingForecastResult.individualChargeForecastResults(),
            billingForecastResult.cumalativeForecastResult());

    return new BillingAnalyticsResult(
        cumalativeHistorical,
        billingForecastSeries,
        pastVariance,
        billingForecastResult
            .cumalativeForecastResult()
            .divide(
                BigDecimal.valueOf(billingForecastResult.forecastSteps()), 4, RoundingMode.HALF_UP),
        highestCostDriver,
        highestCostAcceleration);
  }

  private Map<String, BillingForecastValue> buildBillingForecastSeries(
      Map<String, BigDecimal> forecastSeries, BigDecimal cumalativeForecastValue) {
    return forecastSeries.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry ->
                    new BillingForecastValue(
                        entry.getValue(),
                        entry
                            .getValue()
                            .divide(cumalativeForecastValue, 5, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)),
                        getChargeLabel(entry.getKey()))));
  }

  private String getChargeLabel(String chargeId) {
    // I expect the charge label to maybe be something that requires change, hence it is a method
    // For now this method takes a charge such as i-067d6c946c07882dd%%%AWSDataTransfer and returns
    // AWSDataTransfer (i-067d6c946c07882dd)
    String[] splitChargeId = chargeId.split("%%%");

    StringBuilder chargeLabel = new StringBuilder();

    chargeLabel.append(splitChargeId[1]).append(" (");

    if (splitChargeId[0].equals("null")) {
      chargeLabel.append("No resource");
    } else {
      chargeLabel.append(splitChargeId[0]);
    }

    chargeLabel.append(")");

    return chargeLabel.toString();
  }

  private BigDecimal variance(BigDecimal cumalativeHistorical, BigDecimal cumalativeForecast) {

    if (cumalativeForecast.equals(cumalativeHistorical)) {
      logger.info(
          "Past and forecast cumalative billing values exactly the same, returning 0 for variance");
      return BigDecimal.ZERO;
    }

    logger.info("Cumalative historical debug {}", cumalativeHistorical);

    BigDecimal absoluteDifference = cumalativeForecast.subtract(cumalativeHistorical).abs();

    logger.info("Absolute difference {}", absoluteDifference);
    BigDecimal average = cumalativeForecast.divide(absoluteDifference, 5, RoundingMode.HALF_UP);
    return absoluteDifference
        .divide(average, 5, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100.0));
  }

  private BigDecimal getCumalativePast(Instant firstForecastDate, Integer forecastSteps) {
    // Assumptions:
    // Forecast remain at daily granularity

    Instant to = firstForecastDate.minusSeconds(DAY_IN_SECONDS);
    // cast to long to prevent int overflow
    Instant from = to.minusSeconds((long) DAY_IN_SECONDS * forecastSteps);

    logger.info("Historical from {} Historical to {}", from, to);

    return normalizedCostsRepository.sumTotalCostBetween(
        OffsetDateTime.ofInstant(from, ZoneOffset.UTC),
        OffsetDateTime.ofInstant(to, ZoneOffset.UTC));
  }

  private String primaryCostDriver(Map<String, BigDecimal> chargeSeries) {
    return getMaxChargeValue(chargeSeries);
  }

  private String highestCostAcceleration(Map<String, List<BigDecimal>> chargeSeries) {
    Map<String, BigDecimal> chargeHighestCostAccelerations =
        chargeSeries.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, entry -> highestSeriesCostAcceleration(entry.getValue())));

    return getMaxChargeValue(chargeHighestCostAccelerations);
  }

  private BigDecimal highestSeriesCostAcceleration(List<BigDecimal> series) {

    if (series.size() < 3) {
      logger.info("Too little values in series to calculate cost acceleration");
      return null;
    }

    BigDecimal costAcceleration = null;

    // Formula: where Ci is the the cost at position i in the series
    // acceleration = Ci - 2Ci-1 + Ci-2
    // derived from the velocity and acceleration formulas

    for (int i = 2; i < series.size(); i++) {
      BigDecimal currentAcceleration =
          calculateCostAcceleration(series.get(i), series.get(i - 1), series.get(i - 2));

      costAcceleration =
          costAcceleration == null
              ? currentAcceleration
              : costAcceleration.max(currentAcceleration);
    }

    return costAcceleration;
  }

  private BigDecimal calculateCostAcceleration(
      BigDecimal latestCost, BigDecimal priorCost, BigDecimal twoPeriodsAgoCost) {
    return latestCost.subtract(priorCost.multiply(BigDecimal.valueOf(2))).add(twoPeriodsAgoCost);
  }

  private <V extends Comparable<V>> String getMaxChargeValue(Map<String, V> map) {
    return map.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }
}
