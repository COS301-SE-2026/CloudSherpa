package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.intelligence.service.billing.BillingAnalyticsResult;
import com.cloudsherpa.service.intelligence.service.billing.BillingAnalyticsService;
import com.cloudsherpa.service.intelligence.service.billing.BillingForecastResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingAnalyticsServiceTest {

  @Mock NormalizedCostsRepository normalizedCostsRepository;

  @InjectMocks BillingAnalyticsService analyticsService;

  @Test
  void processCalculatesAnalytics() {
    BillingAnalyticsService service = new BillingAnalyticsService(normalizedCostsRepository);

    when(normalizedCostsRepository.sumTotalCostBetween(any(), any()))
        .thenReturn(BigDecimal.valueOf(100));

    BillingForecastResult forecastResult =
        new BillingForecastResult(
            BigDecimal.valueOf(200),
            Map.of(
                "i-123%%%EC2", BigDecimal.valueOf(150),
                "null%%%AWSDataTransfer", BigDecimal.valueOf(50)),
            Map.of(
                "i-123%%%EC2",
                List.of(
                    BigDecimal.ONE,
                    BigDecimal.ONE,
                    BigDecimal.ONE,
                    BigDecimal.ONE,
                    BigDecimal.valueOf(2),
                    BigDecimal.valueOf(2),
                    BigDecimal.valueOf(2),
                    BigDecimal.valueOf(2),
                    BigDecimal.valueOf(5),
                    BigDecimal.valueOf(5),
                    BigDecimal.valueOf(5),
                    BigDecimal.valueOf(5))),
            List.of(),
            Instant.parse("2026-09-03T00:00:00Z"),
            4);

    BillingAnalyticsResult result = service.process(forecastResult, 4);

    assertEquals(BigDecimal.valueOf(100), result.cumalitivePastForecastValue());
    assertEquals(BigDecimal.valueOf(100).setScale(5), result.pastVariance());
    assertEquals("i-123%%%EC2", result.highestCostDriver());
  }
}
