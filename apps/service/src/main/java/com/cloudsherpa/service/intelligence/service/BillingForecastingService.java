package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastIndividualChargesRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastResponseDto;
import java.math.BigDecimal;
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

  private final Logger logger = LoggerFactory.getLogger(BillingForecastingService.class);

  public BillingForecastingService(
      NormalizedCostsRepository normalizedCostsRepository, RestClient restClient, Sampler sampler) {
    super(restClient, sampler);
    this.normalizedCostsRepository = normalizedCostsRepository;
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
      List<TimestampedNumericDataPoint> chargeSeries =
          normalizedCostsRepository.getTimestampedBillingValues(
              chargeId, PageRequest.of(0, CONTEXT_LENGTH));

      SanatizedSeries sanatizedSeries = sampler.sample(chargeSeries, false);

      if (sanatizedSeries.timestampedNumericDataPoints().size() < 3) {
        logger.info("Could not forecast for charge {} due to insufficient data", chargeId);
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
}
