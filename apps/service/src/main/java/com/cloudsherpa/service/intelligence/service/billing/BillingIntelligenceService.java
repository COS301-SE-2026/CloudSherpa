package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.service.intelligence.dto.BillingForecastIndividualChargesRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequest;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BillingIntelligenceService {

  BillingForecastingService billingForecastingService;

  public BillingIntelligenceService(BillingForecastingService billingForecastingService) {
    this.billingForecastingService = billingForecastingService;
  }

  public BillingForecastResponseDto processAllCharges(
      BillingForecastRequest request, Instant timeOfRequest) {
    BillingForecastResult billingForecastResult =
        billingForecastingService.forecastBillingByAllNonCreditCharges(request, timeOfRequest);

    return toBillingForecastResponseDto(billingForecastResult);
  }

  public BillingForecastResponseDto processSelectCharges(
      BillingForecastIndividualChargesRequestDto request, Instant timeOfRequest) {
    BillingForecastResult billingForecastResult =
        billingForecastingService.forecastBillingByIndividualCharges(request, timeOfRequest);

    return toBillingForecastResponseDto(billingForecastResult);
  }

  // TEMPORARY ANALYTICS STUBS
  private BillingForecastResponseDto toBillingForecastResponseDto(BillingForecastResult result) {
    Map<String, BillingForecastValue> billingForecastSeries = new HashMap<>();
    result
        .individualChargeForecastResults()
        .forEach(
            (chargeId, value) ->
                billingForecastSeries.put(
                    chargeId, new BillingForecastValue(value, BigDecimal.ZERO, chargeId)));

    return new BillingForecastResponseDto(
        result.cumalativeForecastResult(),
        BigDecimal.ZERO,
        billingForecastSeries,
        result.failedForecastCharges(),
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        null);
  }
}
