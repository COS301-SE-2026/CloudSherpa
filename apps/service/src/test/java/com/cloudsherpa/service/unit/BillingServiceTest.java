package com.cloudsherpa.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.billing.dto.BillingKpiRequest;
import com.cloudsherpa.service.billing.dto.BillingKpiResponse;
import com.cloudsherpa.service.billing.service.BillingService;
import com.cloudsherpa.service.config.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {
  // define a fake user that is logged in
  private static final String TENANT_ID = "123e4567-e89b-12d3-a456-426614174000";

  @Mock private NormalizedCostsRepository normalizedCostsRepository;
  @Mock private CloudAccountRepository cloudAccountRepository;

  @Mock private EntityManager entityManager;
  @Mock private Query query;

  @InjectMocks private BillingService billingService;

  @BeforeEach
  void setUp() {
    // log in as the fake user
    TenantContext.setCurrentTenant(TENANT_ID);

    when(entityManager.createNativeQuery(
            "SET search_path TO tenant_123e4567_e89b_12d3_a456_426614174000, public"))
        .thenReturn(query);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void previewKpiReturnsTotalsForAllCharges() {

    OffsetDateTime from = OffsetDateTime.parse("2026-04-01T00:00:00Z");
    OffsetDateTime to = OffsetDateTime.parse("2026-04-08T00:00:00Z");
    BigDecimal current = new BigDecimal("125.50");
    BigDecimal previous = new BigDecimal("100.25");
    OffsetDateTime ingestionTime = OffsetDateTime.parse("2026-04-08T01:00:00Z");

    // When the service asks for the total cost between these dates, return 125.50.
    when(normalizedCostsRepository.sumTotalCostBetween(from, to)).thenReturn(current);
    when(normalizedCostsRepository.sumTotalCostBetween(from.minusDays(7), from))
        .thenReturn(previous);
    when(cloudAccountRepository.findLatestBillingIngestionByUserId(UUID.fromString(TENANT_ID)))
        .thenReturn(ingestionTime);

    BillingKpiResponse response =
        billingService.previewKpi(
            new BillingKpiRequest(null, from.toString(), to.toString(), "weekly"));

    assertEquals(current, response.value());
    assertEquals("USD", response.currency());
    assertEquals(0, response.selectedChargeCount());
    assertEquals("Last 7 days", response.timeLabel());
    assertEquals(ingestionTime.toString(), response.updatedAt());
    assertEquals(previous, response.previousValue());

    verify(query).executeUpdate();
  }

  @Test
  void previewKpiUsesSelectedChargesForCurrentAndPreviousPeriods() {

    OffsetDateTime from = OffsetDateTime.parse("2026-04-01T00:00:00Z");
    OffsetDateTime to = OffsetDateTime.parse("2026-04-08T00:00:00Z");
    List<String> chargeIds = List.of("charge-a", "charge-b");

    when(normalizedCostsRepository.sumTotalCostBetweenForResources(from, to, chargeIds))
        .thenReturn(new BigDecimal("75.00"));
    when(normalizedCostsRepository.sumTotalCostBetweenForResources(
            from.minusDays(7), from, chargeIds))
        .thenReturn(new BigDecimal("50.00"));
    when(cloudAccountRepository.findLatestBillingIngestionByUserId(UUID.fromString(TENANT_ID)))
        .thenReturn(null);

    BillingKpiResponse response =
        billingService.previewKpi(
            new BillingKpiRequest(chargeIds, from.toString(), to.toString(), "monthly"));

    assertEquals(new BigDecimal("75.00"), response.value());
    assertEquals(2, response.selectedChargeCount());
    assertEquals("Last 30 days", response.timeLabel());
    assertEquals("null", response.updatedAt());
    assertEquals(new BigDecimal("50.00"), response.previousValue());

    verify(normalizedCostsRepository).sumTotalCostBetweenForResources(from, to, chargeIds);
    verify(normalizedCostsRepository)
        .sumTotalCostBetweenForResources(from.minusDays(7), from, chargeIds);
  }

  @Test
  void emptyChargeIdsAreTreatedAsAllCharges() {
    OffsetDateTime from = OffsetDateTime.parse("2026-04-01T00:00:00Z");
    OffsetDateTime to = OffsetDateTime.parse("2026-04-02T00:00:00Z");

    when(normalizedCostsRepository.sumTotalCostBetween(any(), any())).thenReturn(BigDecimal.TEN);

    BillingKpiResponse response =
        billingService.previewKpi(
            new BillingKpiRequest(List.of(), from.toString(), to.toString(), null));

    assertEquals(0, response.selectedChargeCount());
    assertEquals("Custom range", response.timeLabel());

    verify(normalizedCostsRepository, times(2)).sumTotalCostBetween(any(), any());

    verify(normalizedCostsRepository, never())
        .sumTotalCostBetweenForResources(any(), any(), anyList());
  }

  @ParameterizedTest
  @CsvSource({
    "daily,Last 24 hours",
    "weekly,Last 7 days",
    "monthly,Last 30 days",
    "unknown,Custom range",
    "'',Custom range"
  })
  void previewKpiResolvesAggregationLabel(String aggregation, String expectedLabel) {
    OffsetDateTime from = OffsetDateTime.parse("2026-04-01T00:00:00Z");
    OffsetDateTime to = OffsetDateTime.parse("2026-04-02T00:00:00Z");

    when(normalizedCostsRepository.sumTotalCostBetween(any(), any())).thenReturn(BigDecimal.ZERO);

    BillingKpiResponse response =
        billingService.previewKpi(
            new BillingKpiRequest(null, from.toString(), to.toString(), aggregation));

    assertEquals(expectedLabel, response.timeLabel());
  }
}
