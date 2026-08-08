package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastRequestDto;
import com.cloudsherpa.service.intelligence.dto.IntelligenceForecastResponseDto;
import java.math.BigDecimal;
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

  public BillingForecastResponseDto forecastBilling(
      BillingForecastRequestDto billingForecastRequestDto) {
    BigDecimal totalCostForecast = BigDecimal.valueOf(0);
    Map<String, BigDecimal> individualChargeForecasts = new HashMap<>();
    for (String chargeId : billingForecastRequestDto.chargeIds()) {
      List<TimestampedNumericDataPoint> chargeSeries =
          normalizedCostsRepository.getTimestampedBillingValues(
              chargeId, PageRequest.of(0, CONTEXT_LENGTH));

      SanatizedSeries sanatizedSeries = sampler.sample(chargeSeries, false);

      debugDataPointLog(sanatizedSeries.timestampedNumericDataPoints());

      int forecastHorizon = Math.abs(Math.toIntExact(2_592_000L / sanatizedSeries.periodicity()));

      IntelligenceForecastRequestDto intelligenceForecastRequestDto =
          constructForecastRequest(chargeSeries, forecastHorizon);
      IntelligenceForecastResponseDto intelligenceForecastResponseDto =
          makeForecastRequest(intelligenceForecastRequestDto);

      BigDecimal aggregatedCharge = BigDecimal.valueOf(0);

      for (BigDecimal forecastedChargePoint : intelligenceForecastResponseDto.forecast()) {
        aggregatedCharge = aggregatedCharge.add(forecastedChargePoint);
      }

      logger.info("Forecasted charge for {} is {}", chargeId, aggregatedCharge);

      totalCostForecast = totalCostForecast.add(aggregatedCharge);
      individualChargeForecasts.put(chargeId, aggregatedCharge);
    }

    logger.info("Total forecasted cost {}", totalCostForecast);

    return null;
  }
}
