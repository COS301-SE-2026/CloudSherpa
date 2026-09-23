package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.lib.repositories.BillingForecastRepository;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BillingForecastStateService {
  private final BillingForecastRepository billingForecastRepository;

  public BillingForecastStateService(BillingForecastRepository billingForecastRepository) {
    this.billingForecastRepository = billingForecastRepository;
  }

  public void writeForecast(
      UUID tenantId, UUID forecastRunId, BillingForecastResponseDto forecast) {
    // write
  }
}
