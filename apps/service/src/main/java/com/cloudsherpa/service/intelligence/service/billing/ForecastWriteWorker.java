package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.entities.BillingForecast;
import com.cloudsherpa.lib.entities.BillingForecastRun;
import com.cloudsherpa.lib.entities.ForecastExecutionStatusEnum;
import com.cloudsherpa.lib.repositories.BillingForecastRepository;
import com.cloudsherpa.lib.repositories.BillingForecastRunRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ForecastWriteWorker {
  private final BillingForecastRepository forecastRepository;
  private final BillingForecastRunRepository runRepository;
  private final ObjectMapper objectMapper;

  ForecastWriteWorker(
      BillingForecastRepository forecastRepository,
      BillingForecastRunRepository runRepository,
      ObjectMapper objectMapper) {
    this.forecastRepository = forecastRepository;
    this.runRepository = runRepository;
    this.objectMapper = objectMapper;
  }

  // The tenant context has to have been set within the scope where these methods are called
  @Transactional
  public void writeForecast(
      UUID forecastRunId,
      BillingForecastResponseDto forecast,
      OffsetDateTime timestamp,
      int forecastWindow) {
    BillingForecast newForecast = toForecast(forecastRunId, forecast, timestamp, forecastWindow);
    forecastRepository.save(newForecast);
  }

  @Transactional
  public BillingForecastRun intializeForecastRun(Instant timestamp) {
    BillingForecastRun newRun =
        new BillingForecastRun(
            UUID.randomUUID(), ForecastExecutionStatusEnum.PROCESSING, timestamp);
    return runRepository.save(newRun);
  }

  @Transactional
  public void updateForecastRun(BillingForecastRun forecastRun) {
    runRepository.save(forecastRun);
  }

  private BillingForecast toForecast(
      UUID forecastRunId,
      BillingForecastResponseDto forecast,
      OffsetDateTime timestamp,
      int forecastWindow) {
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
