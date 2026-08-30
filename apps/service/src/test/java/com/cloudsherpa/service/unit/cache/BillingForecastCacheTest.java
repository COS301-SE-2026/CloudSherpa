package com.cloudsherpa.service.unit.cache;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.service.config.CacheConfig;
import com.cloudsherpa.service.intelligence.dto.BillingForecastRequest;
import com.cloudsherpa.service.intelligence.service.billing.BillingAnalyticsResult;
import com.cloudsherpa.service.intelligence.service.billing.BillingAnalyticsService;
import com.cloudsherpa.service.intelligence.service.billing.BillingForecastResult;
import com.cloudsherpa.service.intelligence.service.billing.BillingForecastValue;
import com.cloudsherpa.service.intelligence.service.billing.BillingForecastingService;
import com.cloudsherpa.service.intelligence.service.billing.BillingIntelligenceService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      CacheConfig.class,
      BillingIntelligenceService.class,
      BillingForecastCacheTest.TestConfig.class
    })
class BillingForecastCacheTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    CacheManager cacheManager() {
      return new CaffeineCacheManager("billing-forecast");
    }
  }

  @Autowired CacheManager cacheManager;

  @Autowired BillingIntelligenceService billingIntelligenceService;

  @MockitoBean BillingForecastingService billingForecastingService;

  @MockitoBean BillingAnalyticsService billingAnalyticsService;

  @BeforeEach
  void clearCache() {
    cacheManager.getCache("billing-forecast").clear();
  }

  @Test
  void cachesForecast() {
    BillingForecastRequest request = new BillingForecastRequest(30);
    Instant timeOfRequest = Instant.now();

    when(billingForecastingService.forecastBillingByAllNonCreditCharges(request, timeOfRequest))
        .thenReturn(expectedBillingForecastResult());
    when(billingAnalyticsService.process(expectedBillingForecastResult(), 30))
        .thenReturn(expectedBillingAnalyticsResult());

    billingIntelligenceService.processAllCharges(
        request, timeOfRequest, UUID.fromString("a5b9f5d2-5cdb-4f29-be84-5bc5a7ab5529"));
    billingIntelligenceService.processAllCharges(
        request, timeOfRequest, UUID.fromString("a5b9f5d2-5cdb-4f29-be84-5bc5a7ab5529"));

    verify(billingForecastingService, times(1))
        .forecastBillingByAllNonCreditCharges(request, timeOfRequest);
    verify(billingAnalyticsService, times(1)).process(expectedBillingForecastResult(), 30);
  }

  @Test
  void doesNotCacheForDifferentUser() {
    BillingForecastRequest request = new BillingForecastRequest(30);
    Instant timeOfRequest = Instant.now();

    when(billingForecastingService.forecastBillingByAllNonCreditCharges(request, timeOfRequest))
        .thenReturn(expectedBillingForecastResult());
    when(billingAnalyticsService.process(expectedBillingForecastResult(), 30))
        .thenReturn(expectedBillingAnalyticsResult());

    billingIntelligenceService.processAllCharges(
        request, timeOfRequest, UUID.fromString("a5b9f5d2-5cdb-4f29-be84-5bc5a7ab5529"));
    billingIntelligenceService.processAllCharges(
        request, timeOfRequest, UUID.fromString("a5b9f5d2-5cdb-4f29-be84-5bc5a7ab5629"));

    verify(billingForecastingService, times(2))
        .forecastBillingByAllNonCreditCharges(request, timeOfRequest);
    verify(billingAnalyticsService, times(2)).process(expectedBillingForecastResult(), 30);
  }

  private BillingForecastResult expectedBillingForecastResult() {
    return new BillingForecastResult(
        BigDecimal.valueOf(42.50),
        Map.of("charge-id-01", BigDecimal.valueOf(42.50)),
        Map.of("charge-id-01", List.of(BigDecimal.valueOf(42.50))),
        List.of(),
        Instant.parse("2026-08-29T00:00:00Z"),
        30);
  }

  private BillingAnalyticsResult expectedBillingAnalyticsResult() {
    return new BillingAnalyticsResult(
        BigDecimal.valueOf(30.00),
        Map.of(
            "charge-id-01",
            new BillingForecastValue(
                BigDecimal.valueOf(42.50), BigDecimal.valueOf(100.00), "charge-id-01")),
        BigDecimal.valueOf(12.50),
        BigDecimal.valueOf(1.42),
        "charge-id-01",
        "charge-id-01");
  }
}
