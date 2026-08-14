package com.cloudsherpa.service.intelligence.service.billing;

import com.cloudsherpa.service.intelligence.dto.BillingForecastIndividualChargesRequestDto;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequest;
import com.cloudsherpa.service.intelligence.dto.BillingForecastResponseDto;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class BillingIntelligenceService {

  BillingForecastingService billingForecastingService;
  BillingAnalyticsService billingAnalyticsService;

  public BillingIntelligenceService(
      BillingForecastingService billingForecastingService,
      BillingAnalyticsService billingAnalyticsService) {
    this.billingForecastingService = billingForecastingService;
    this.billingAnalyticsService = billingAnalyticsService;
  }

  public BillingForecastResponseDto processAllCharges(
      BillingForecastRequest request, Instant timeOfRequest) {
    BillingForecastResult billingForecastResult =
        billingForecastingService.forecastBillingByAllNonCreditCharges(request, timeOfRequest);

    BillingAnalyticsResult billingAnalyticsResult =
        billingAnalyticsService.process(billingForecastResult, request.forecastSteps());
    return toBillingForecastResponseDto(billingForecastResult, billingAnalyticsResult);
  }

  public BillingForecastResponseDto processSelectCharges(
      BillingForecastIndividualChargesRequestDto request, Instant timeOfRequest) {
    BillingForecastResult billingForecastResult =
        billingForecastingService.forecastBillingByIndividualCharges(request, timeOfRequest);

    BillingAnalyticsResult billingAnalyticsResult =
        billingAnalyticsService.process(billingForecastResult, request.forecastSteps());
    return toBillingForecastResponseDto(billingForecastResult, billingAnalyticsResult);
  }

  // TEMPORARY ANALYTICS STUBS
  private BillingForecastResponseDto toBillingForecastResponseDto(
      BillingForecastResult forecastResult, BillingAnalyticsResult analyticsResult) {

    return new BillingForecastResponseDto(
        forecastResult.cumalativeForecastResult(),
        analyticsResult.cumalitivePastForecastValue(),
        analyticsResult.billingForecastSeries(),
        forecastResult.failedForecastCharges(),
        analyticsResult.pastVariance(),
        analyticsResult.dailyBurnRate(),
        analyticsResult.highestCostDriver(),
        analyticsResult.highestCostAcceleration());
  }
}
