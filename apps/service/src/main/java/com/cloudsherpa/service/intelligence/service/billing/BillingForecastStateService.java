package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.entities.BillingForecast;
import com.cloudsherpa.lib.entities.BillingForecastRun;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BillingForecastStateService {
  // Callers must establish tenant context before invoking this service.
  private final ForecastReadWriteWorker worker;

  public BillingForecastStateService(ForecastReadWriteWorker worker) {
    this.worker = worker;
  }

  public void writeForecast(
      UUID forecastRunId,
      BillingForecastResponseDto forecast,
      OffsetDateTime timestamp,
      int forecastWindow) {
    worker.writeForecast(forecastRunId, forecast, timestamp, forecastWindow);
  }

  public BillingForecastRun intializeForecastRun(Instant timestamp) {
    return worker.intializeForecastRun(timestamp);
  }

  public void updateForecastRun(BillingForecastRun forecastRun) {
    worker.updateForecastRun(forecastRun);
  }

  public BillingForecast readLatestForecast(Integer forecastWindow) {
    BillingForecastRun latestCompletedRun = worker.latestCompletedForecastRun();
    if (latestCompletedRun == null) {
      return null;
    }
    return worker.getForecastForRunAndWindow(latestCompletedRun.getForecastRunId(), forecastWindow);
  }
}
