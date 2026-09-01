package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastIndividualChargesRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequest;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.SanatizedSeries;
import com.cloudsherpa.service.intelligence.dto.SanitizedChargeSeries;
import com.cloudsherpa.service.intelligence.registry.ChargeProviderRegistry;
import com.cloudsherpa.service.intelligence.service.ForecastingService;
import com.cloudsherpa.service.intelligence.service.Sampler;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BillingForecastingService extends ForecastingService {
  private final NormalizedCostsRepository normalizedCostsRepository;
  private final ChargeProviderRegistry chargeProviderRegistry;
  private final Logger logger = LoggerFactory.getLogger(BillingForecastingService.class);

  // Threshold to account for billing latency in reports, i.e. most recent report does not contain
  // all up to date charges
  private static final int OLD_CHARGE_CUTOFF_DAYS = 60;
  private static final long DAY_IN_SECONDS = 86_400;

  public BillingForecastingService(
      NormalizedCostsRepository normalizedCostsRepository,
      RestClient restClient,
      Sampler sampler,
      ChargeProviderRegistry chargeProviderRegistry) {
    super(restClient, sampler);
    this.normalizedCostsRepository = normalizedCostsRepository;
    this.chargeProviderRegistry = chargeProviderRegistry;
  }

  public BillingForecastResult forecastBillingByIndividualCharges(
      BillingForecastIndividualChargesRequestDto request, Instant timeOfRequest) {
    return executeBillingForecast(request.chargeIds(), timeOfRequest, request.forecastSteps());
  }

  public BillingForecastResult forecastBillingByAllNonCreditCharges(
      BillingForecastRequest request, Instant timeOfRequest) {
    List<String> chargeIds = normalizedCostsRepository.findDistinctChargeIdsNonCredit();
    return executeBillingForecast(chargeIds, timeOfRequest, request.forecastSteps());
  }

  private BillingForecastResult executeBillingForecast(
      List<String> chargeIds, Instant timeOfRequest, int forecastSteps) {
    BigDecimal totalCostForecast = BigDecimal.valueOf(0);
    Map<String, BigDecimal> individualChargeForecasts = new HashMap<>();
    Map<String, List<BigDecimal>> chargeSeries = new HashMap<>();
    List<String> failedForecastCharges = new ArrayList<>();
    for (String chargeId : chargeIds) {
      logger.info(chargeId);
      SanitizedChargeSeries sanatizedSeries =
          sanitizedChargeSeries(chargeId, timeOfRequest, forecastSteps);

      if (sanatizedSeries == null || sanatizedSeries.timestampedNumericDataPoints().size() < 3) {
        logger.info(
            "Could not forecast for charge {} due to insufficient or outdated data", chargeId);
        failedForecastCharges.add(chargeId);
        continue;
      }

      IntelligenceForecastRequestDto intelligenceForecastRequestDto =
          constructForecastRequest(
              sanatizedSeries.timestampedNumericDataPoints(), sanatizedSeries.forecastSteps());
      IntelligenceForecastResponseDto intelligenceForecastResponseDto =
          makeForecastRequest(intelligenceForecastRequestDto);

      BigDecimal aggregatedCharge = BigDecimal.ZERO;

      for (int i = 0; i < intelligenceForecastResponseDto.forecast().size(); i++) {
        LocalDateTime forecastTimestamp = intelligenceForecastResponseDto.timestamps().get(i);

        if (!forecastTimestamp.isBefore(LocalDateTime.ofInstant(timeOfRequest, ZoneOffset.UTC))) {
          BigDecimal currentForecastValue = intelligenceForecastResponseDto.forecast().get(i);
          aggregatedCharge = aggregatedCharge.add(currentForecastValue);
          chargeSeries
              .computeIfAbsent(chargeId, k -> new ArrayList<BigDecimal>())
              .add(currentForecastValue);
        }
      }

      // Cap at 0
      aggregatedCharge = aggregatedCharge.max(BigDecimal.valueOf(0));

      logger.info("Forecasted charge for {} is {}", chargeId, aggregatedCharge);

      totalCostForecast = totalCostForecast.add(aggregatedCharge);
      individualChargeForecasts.put(chargeId, aggregatedCharge);
    }

    logger.info("Total forecasted cost {}", totalCostForecast);

    return new BillingForecastResult(
        totalCostForecast,
        individualChargeForecasts,
        chargeSeries,
        failedForecastCharges,
        timeOfRequest,
        forecastSteps);
  }

  private SanitizedChargeSeries sanitizedChargeSeries(
      String chargeId, Instant timeOfRequest, int forecastSteps) {
    List<TimestampedNumericDataPoint> chargeSeries =
        normalizedCostsRepository.getTimestampedBillingValues(
            chargeId, PageRequest.of(0, CONTEXT_LENGTH));

    if (chargeSeries.isEmpty()) {
      return null;
    }

    ProviderEnum chargeProvider = chargeProviderRegistry.getChargeProvider(chargeId);
    Instant mostRecentBillingIngestionDate =
        normalizedCostsRepository.findLatestProviderBillingReportDate(chargeProvider);

    Duration timeBetweenLatestIngestion =
        Duration.between(chargeSeries.getLast().timestamp(), mostRecentBillingIngestionDate);

    if (timeBetweenLatestIngestion.toDays() > OLD_CHARGE_CUTOFF_DAYS) {
      logger.info(
          "Charge {} was last ingested before the defined threshold and is thus not forecasted",
          chargeId);
      return null;
    }

    SanatizedSeries sanatizedSeries =
        sampler.sample(
            chargeSeries, true, mostRecentBillingIngestionDate.minusSeconds(DAY_IN_SECONDS));

    Duration timeBetweenLatestIngestionAndRequest =
        Duration.between(mostRecentBillingIngestionDate, timeOfRequest);

    if (sanatizedSeries.periodicity() == 0) {
      return null;
    }

    int calculatedForecastSteps =
        calculateForecastSteps(
            timeBetweenLatestIngestion,
            timeBetweenLatestIngestionAndRequest,
            forecastSteps,
            sanatizedSeries.periodicity());

    return new SanitizedChargeSeries(
        sanatizedSeries.timestampedNumericDataPoints(),
        sanatizedSeries.periodicity(),
        calculatedForecastSteps);
  }

  private int calculateForecastSteps(
      Duration timeBetweenLatestIngestion,
      Duration timeBetweenLatestIngestionAndRequest,
      int forecastSteps,
      long periodicity) {
    return Math.abs(
        Math.toIntExact(
            (DAY_IN_SECONDS * forecastSteps
                    + (timeBetweenLatestIngestion.toSeconds()
                        + Math.max(timeBetweenLatestIngestionAndRequest.toSeconds(), 0)))
                / periodicity));
  }
}
