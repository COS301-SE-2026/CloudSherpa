package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastIndividualChargesRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequest;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.SanatizedSeries;
import com.cloudsherpa.service.intelligence.dto.SanitizedChargeSeries;
import com.cloudsherpa.service.intelligence.registry.ChargeProviderRegistry;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
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
  private static final int OLD_CHARGE_CUTOFF_DAYS = 2;

  public BillingForecastingService(
      NormalizedCostsRepository normalizedCostsRepository,
      RestClient restClient,
      Sampler sampler,
      ChargeProviderRegistry chargeProviderRegistry) {
    super(restClient, sampler);
    this.normalizedCostsRepository = normalizedCostsRepository;
    this.chargeProviderRegistry = chargeProviderRegistry;
  }

  public BillingForecastResponseDto forecastBillingByIndividualCharges(
      BillingForecastIndividualChargesRequestDto request, Instant timeOfRequest) {
    return executeBillingForecast(request.chargeIds(), timeOfRequest, request.forecastSteps());
  }

  public BillingForecastResponseDto forecastBillingByAllNonCreditCharges(
      BillingForecastRequest request, Instant timeOfRequest) {
    List<String> chargeIds = normalizedCostsRepository.findDistinctChargeIdsNonCredit();
    return executeBillingForecast(chargeIds, timeOfRequest, request.forecastSteps());
  }

  private BillingForecastResponseDto executeBillingForecast(
      List<String> chargeIds, Instant timeOfRequest, int forecastSteps) {
    BigDecimal totalCostForecast = BigDecimal.valueOf(0);
    Map<String, BigDecimal> individualChargeForecasts = new HashMap<>();
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

      BigDecimal aggregatedCharge = BigDecimal.valueOf(0);

      for (BigDecimal forecastedChargePoint : intelligenceForecastResponseDto.forecast()) {
        aggregatedCharge = aggregatedCharge.add(forecastedChargePoint);
      }

      // Cap at 0
      aggregatedCharge = aggregatedCharge.max(BigDecimal.valueOf(0));

      logger.info("Forecasted charge for {} is {}", chargeId, aggregatedCharge);

      totalCostForecast = totalCostForecast.add(aggregatedCharge);
      individualChargeForecasts.put(chargeId, aggregatedCharge);
    }

    logger.info("Total forecasted cost {}", totalCostForecast);

    return new BillingForecastResponseDto(
        totalCostForecast, individualChargeForecasts, failedForecastCharges);
  }

  private SanitizedChargeSeries sanitizedChargeSeries(
      String chargeId, Instant timeOfRequest, int forecastSteps) {
    List<TimestampedNumericDataPoint> chargeSeries =
        normalizedCostsRepository.getTimestampedBillingValues(
            chargeId, PageRequest.of(0, CONTEXT_LENGTH));

    ProviderEnum chargeProvider = chargeProviderRegistry.getChargeProvider(chargeId);
    Instant mostRecentBillingIngestionDate =
        normalizedCostsRepository.findLatestProviderBillingReportDate(chargeProvider);

    Duration timeBetweenLatestIngestion =
        Duration.between(chargeSeries.getLast().timestamp(), mostRecentBillingIngestionDate);

    if (timeBetweenLatestIngestion.toDays() < OLD_CHARGE_CUTOFF_DAYS) {
      SanatizedSeries sanatizedSeries = sampler.sample(chargeSeries, false);

      Duration timeBetweenLatestIngestionAndRequest =
          Duration.between(mostRecentBillingIngestionDate, timeOfRequest);

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
    } else {
      logger.info(
          "Charge {} was last ingested before the defined threshold and is thus not forecasted",
          chargeId);
      return null;
    }
  }

  private int calculateForecastSteps(
      Duration timeBetweenLatestIngestion,
      Duration timeBetweenLatestIngestionAndRequest,
      int forecastSteps,
      long periodicity) {
    return Math.abs(
        Math.toIntExact(
            (86_400 * forecastSteps
                    + (timeBetweenLatestIngestion.toSeconds()
                        + timeBetweenLatestIngestionAndRequest.toSeconds()))
                / periodicity));
  }
}
