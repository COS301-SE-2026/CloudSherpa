package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastIndividualChargesRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastResponseDto;
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
      BillingForecastIndividualChargesRequestDto billingForecastIndividualChargesRequestDto) {
    return executeBillingForecast(billingForecastIndividualChargesRequestDto.chargeIds());
  }

  public BillingForecastResponseDto forecastBillingByAllNonCreditCharges() {
    List<String> chargeIds = normalizedCostsRepository.findDistinctChargeIdsNonCredit();
    return executeBillingForecast(chargeIds);
  }

  private BillingForecastResponseDto executeBillingForecast(List<String> chargeIds) {
    BigDecimal totalCostForecast = BigDecimal.valueOf(0);
    Map<String, BigDecimal> individualChargeForecasts = new HashMap<>();
    List<String> failedForecastCharges = new ArrayList<>();
    for (String chargeId : chargeIds) {
      logger.info(chargeId);
      SanatizedSeries sanatizedSeries = sanitizedChargeSeries(chargeId);

      if (sanatizedSeries == null || sanatizedSeries.timestampedNumericDataPoints().size() < 3) {
        logger.info(
            "Could not forecast for charge {} due to insufficient or outdated data", chargeId);
        failedForecastCharges.add(chargeId);
        continue;
      }

      int forecastHorizon = Math.abs(Math.toIntExact(2_592_000L / sanatizedSeries.periodicity()));

      IntelligenceForecastRequestDto intelligenceForecastRequestDto =
          constructForecastRequest(sanatizedSeries.timestampedNumericDataPoints(), forecastHorizon);
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

  private SanatizedSeries sanitizedChargeSeries(String chargeId) {
    List<TimestampedNumericDataPoint> chargeSeries =
        normalizedCostsRepository.getTimestampedBillingValues(
            chargeId, PageRequest.of(0, CONTEXT_LENGTH));

    ProviderEnum chargeProvider = chargeProviderRegistry.getChargeProvider(chargeId);
    Instant mostRecentBillingIngestionDate =
        normalizedCostsRepository.findLatestProviderBillingReportDate(chargeProvider);

    logger.info("Most recent billing date {}", mostRecentBillingIngestionDate);
    logger.info("Most recent series item {}", chargeSeries.getLast().timestamp());
    if (Duration.between(chargeSeries.getLast().timestamp(), mostRecentBillingIngestionDate)
            .toDays()
        < OLD_CHARGE_CUTOFF_DAYS) {
      logger.info("{} is safe", chargeId);
    } else {
      logger.info("charge {} is not safe", chargeId);
      return null;
    }

    return sampler.sample(chargeSeries, false);
  }
}
