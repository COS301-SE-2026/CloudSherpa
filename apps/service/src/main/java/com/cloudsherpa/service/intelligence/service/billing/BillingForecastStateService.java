package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.entities.BillingForecast;
import com.cloudsherpa.lib.entities.BillingForecastRun;
import com.cloudsherpa.service.config.TenantContext;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BillingForecastStateService {
  private final ForecastReadWriteWorker worker;

  public BillingForecastStateService(ForecastReadWriteWorker worker) {
    this.worker = worker;
  }

  public void writeForecast(
      UUID tenantId,
      UUID forecastRunId,
      BillingForecastResponseDto forecast,
      OffsetDateTime timestamp,
      int forecastWindow) {
    try {
      TenantContext.setCurrentTenant(tenantId.toString());
      worker.writeForecast(forecastRunId, forecast, timestamp, forecastWindow);
    } finally {
      TenantContext.clear();
    }
  }

  public BillingForecastRun intializeForecastRun(UUID tenantId, Instant timestamp) {
    try {
      TenantContext.setCurrentTenant(tenantId.toString());
      return worker.intializeForecastRun(timestamp);
    } finally {
      TenantContext.clear();
    }
  }

  public void updateForecastRun(UUID tenantId, BillingForecastRun forecastRun) {
    try {
      TenantContext.setCurrentTenant(tenantId.toString());
      worker.updateForecastRun(forecastRun);
    } finally {
      TenantContext.clear();
    }
  }

  public BillingForecast readLatestForecast(UUID tenantId, Integer forecastWindow) {
    try {
      TenantContext.setCurrentTenant(tenantId.toString());
      BillingForecastRun latestCompletedRun = worker.latestCompletedForecastRun();
      return worker.getForecastForRunAndWindow(
          latestCompletedRun.getForecastRunId(), forecastWindow);
    } finally {
      TenantContext.clear();
    }
  }
}
