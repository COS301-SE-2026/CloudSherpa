package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.entities.BillingForecast;
import com.cloudsherpa.lib.repositories.BillingForecastRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ForecastWriteWorker {
  private final BillingForecastRepository repository;
  private final ObjectMapper objectMapper;

  ForecastWriteWorker(BillingForecastRepository repository, ObjectMapper objectMapper) {
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  // The tenant context has to have been set within the scope where this method is called
  @Transactional
  public void writeForecast(
      UUID forecsatRunId,
      BillingForecastResponseDto forecast,
      OffsetDateTime timestamp,
      Integer forecastWindow) {
    BillingForecast newForecast = toForecast(forecsatRunId, forecast, timestamp, forecastWindow);
    repository.save(newForecast);
  }

  private BillingForecast toForecast(
      UUID forecastRunId,
      BillingForecastResponseDto forecast,
      OffsetDateTime timestamp,
      Integer forecastWindow) {
    return BillingForecast.builder()
        .accelerationRate(forecast.accelerationRate())
        .cumulativeForecastValue(forecast.cumalativeBillingForecastValue())
        .cumulativePastForecastValue(forecast.cumalitivePastForecastingValue())
        .dailyBurnRate(forecast.dailyBurnRate())
        .failedCharges(forecast.failedForecastCharges())
        .forecastId(UUID.randomUUID())
        .forecastRunId(forecastRunId)
        .forecastSeries(objectMapper.valueToTree(forecast.billingForecastSeries()))
        .forecastTimestamp(timestamp)
        .forecastWindow(forecastWindow)
        .pastVariance(forecast.pastVariance())
        .highestCostDriver(forecast.highestCostDriver())
        .highestCostAcceleration(forecast.highestCostAcceleration())
        .build();
  }
}
