package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.entities.BillingForecast;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.cloudsherpa.service.intelligence.model.ForecastSeries;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class BillingForecastResultService {
  private final BillingForecastStateService stateService;
  private final ObjectMapper objectMapper;

  public BillingForecastResultService(
      BillingForecastStateService stateService, ObjectMapper objectMapper) {
    this.stateService = stateService;
    this.objectMapper = objectMapper;
  }

  public BillingForecastResponseDto latestForecast(int forecastWindow) {
    BillingForecast forecast = stateService.readLatestForecast(forecastWindow);

    if (forecast == null) {
      return null;
    }

    return toResponse(forecast);
  }

  private BillingForecastResponseDto toResponse(BillingForecast forecast) {
    return new BillingForecastResponseDto(
        forecast.getCumulativeForecastValue(),
        forecast.getCumulativePastForecastValue(),
        objectMapper.convertValue(forecast.getForecastSeries(), ForecastSeries.class),
        forecast.getFailedCharges(),
        forecast.getPastVariance(),
        forecast.getDailyBurnRate(),
        forecast.getHighestCostDriver(),
        forecast.getHighestCostAcceleration(),
        forecast.getAccelerationRate());
  }
}
