package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.entities.BillingForecast;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
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
        toForecastSeries(forecast.getForecastSeries()),
        forecast.getFailedCharges(),
        forecast.getPastVariance(),
        forecast.getDailyBurnRate(),
        forecast.getHighestCostDriver(),
        forecast.getHighestCostAcceleration(),
        forecast.getAccelerationRate());
  }

  private Map<String, BillingForecastValue> toForecastSeries(JsonNode storedSeries) {
    JsonNode series =
        storedSeries.has("billingForecastSeries")
            ? storedSeries.get("billingForecastSeries")
            : storedSeries;
    return objectMapper.convertValue(
        series, new TypeReference<Map<String, BillingForecastValue>>() {});
  }
}
