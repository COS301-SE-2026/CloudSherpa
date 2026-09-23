package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.entities.BillingForecastRun;
import com.cloudsherpa.lib.entities.ForecastExecutionStatusEnum;
import com.cloudsherpa.service.config.TenantContext;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequest;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BillingForecastWorker {
  private final BillingForecastStateService stateService;
  private final BillingIntelligenceService billingIntelligenceService;

  private static final List<Integer> SUPPORTED_FORECAST_WINDOWS = List.of(7, 14, 30, 60);

  public BillingForecastWorker(
      BillingForecastStateService stateService,
      BillingIntelligenceService billingIntelligenceService) {
    this.stateService = stateService;
    this.billingIntelligenceService = billingIntelligenceService;
  }

  public void submit(UUID tenantId) {

    BillingForecastRun run = null;

    try {
      TenantContext.setCurrentTenant(tenantId.toString());

      run = stateService.intializeForecastRun(tenantId, Instant.now());

      try {
        for (int forecastWindow : SUPPORTED_FORECAST_WINDOWS) {
          BillingForecastRequest request = new BillingForecastRequest(forecastWindow);
          BillingForecastResponseDto forecast =
              billingIntelligenceService.processAllCharges(request, Instant.now(), tenantId);
          stateService.writeForecast(
              tenantId,
              run.getForecastRunId(),
              forecast,
              OffsetDateTime.now(ZoneId.of("UTC")),
              forecastWindow);
        }

        run.setForecastExecutionStatus(ForecastExecutionStatusEnum.COMPLETED);
        stateService.updateForecastRun(tenantId, run);

      } catch (RuntimeException e) {
        run.setForecastExecutionStatus(ForecastExecutionStatusEnum.FAILED);
        stateService.updateForecastRun(tenantId, run);
      }

    } finally {
      TenantContext.clear();
    }
  }
}
