package com.cloudsherpa.service.unit.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.AlertSeverityEnum;
import com.cloudsherpa.lib.entities.AlertStatusEnum;
import com.cloudsherpa.lib.entities.AlertTypeEnum;
import com.cloudsherpa.lib.entities.Budget;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.lib.repositories.BudgetRepository;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.alerts.service.BudgetEvaluationService;
import com.cloudsherpa.service.sse.SseService;
import com.cloudsherpa.service.webhooks.producers.WebhookProducerService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetEvaluationServiceTest {

  @Mock private NormalizedCostsRepository normalizedCostsRepository;
  @Mock private AlertRepository alertRepository;
  @Mock private SseService sseService;
  @Mock private BudgetRepository budgetRepository;
  @Mock private ResourceRepository resourceRepository;
  @Mock private WebhookProducerService producerService;

  private BudgetEvaluationService service;
  private UUID userId;
  private UUID accountId;

  @BeforeEach
  void setUp() {
    service =
        new BudgetEvaluationService(
            normalizedCostsRepository,
            alertRepository,
            sseService,
            budgetRepository,
            resourceRepository,
            producerService);

    userId = UUID.randomUUID();
    accountId = UUID.randomUUID();
  }

  @Test
  void evaluateForAccountShouldCreateAlertWhenTenantBudgetExceeded() {
    Budget budget = mockBudget(UUID.randomUUID(), "TENANT", null, new BigDecimal(1000.00), 30);

    when(budgetRepository.findByUserIdAndScopeAndEnabledTrue(userId, "TENANT"))
        .thenReturn(List.of(budget));
    when(normalizedCostsRepository.sumTotalCostBetween(
            any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .thenReturn(new BigDecimal(1200.00));
    when(alertRepository.findByCanonicalKeyAndStatus(anyString(), eq(AlertStatusEnum.ACTIVE)))
        .thenReturn(Optional.empty());

    service.evaluateForAccount(userId, accountId);

    ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
    verify(alertRepository).save(captor.capture());

    Alert saved = captor.getValue();
    assertEquals(AlertStatusEnum.ACTIVE, saved.getStatus());
    assertEquals(AlertTypeEnum.BUDGET, saved.getAlertType());
    assertEquals(AlertSeverityEnum.WARNING, saved.getSeverity());
    assertEquals("Current spend exceeded budget of: " + budget.getAmount(), saved.getTitle());
    assertEquals("budget:" + budget.getBudgetId(), saved.getCanonicalKey());

    verify(sseService).broadcast(eq(userId), eq("alert"), same(saved));
    verify(producerService).produceEvent(eq(userId), any(), eq("alert.budget"), any());
  }

  @Test
  void evaluateForAccountShouldSkipAlertWhenSpendIsUnderBudget() {
    Budget budget = mockBudget(UUID.randomUUID(), "ACCOUNT", accountId, new BigDecimal(500.00), 30);

    when(budgetRepository.findByScopeAndScopeIdAndEnabledTrue("ACCOUNT", accountId))
        .thenReturn(List.of(budget));
    when(normalizedCostsRepository.sumTotalCostBetweenForAccountId(
            eq(accountId), any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .thenReturn(new BigDecimal(450.00));

    service.evaluateForAccount(userId, accountId);

    verify(alertRepository, never()).save(any());
    verify(sseService, never()).broadcast(any(), any(), any());
  }

  @Test
  void evaluateForAccountShouldReuseExistingActiveAlertForRepeatViolation() {
    UUID budgetId = UUID.randomUUID();
    Budget budget = mockBudget(budgetId, "TENANT", null, new BigDecimal(1000.00), 30);
    String canonicalKey = "budget:" + budgetId;

    Alert existing =
        Alert.builder()
            .userId(userId)
            .alertType(AlertTypeEnum.BUDGET)
            .severity(AlertSeverityEnum.WARNING)
            .status(AlertStatusEnum.ACTIVE)
            .canonicalKey(canonicalKey)
            .createdAt(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5))
            .build();

    when(budgetRepository.findByUserIdAndScopeAndEnabledTrue(userId, "TENANT"))
        .thenReturn(List.of(budget));
    when(normalizedCostsRepository.sumTotalCostBetween(
            any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .thenReturn(new BigDecimal(1500.00));
    when(alertRepository.findByCanonicalKeyAndStatus(canonicalKey, AlertStatusEnum.ACTIVE))
        .thenReturn(Optional.of(existing));

    service.evaluateForAccount(userId, accountId);

    assertNotNull(existing.getLastSeen());
    assertEquals(new BigDecimal(1500.00), existing.getPayload().get("current_total"));

    verify(alertRepository).save(existing);
    verify(sseService).broadcast(eq(userId), eq("alert"), same(existing));
  }

  @Test
  void evaluateForAccountShouldNotProcessDisabledBudgets() {
    Budget budget = mock(Budget.class);
    when(budget.isEnabled()).thenReturn(false);

    when(budgetRepository.findByUserIdAndScopeAndEnabledTrue(userId, "TENANT"))
        .thenReturn(List.of(budget));

    service.evaluateForAccount(userId, accountId);

    verify(normalizedCostsRepository, never()).sumTotalCostBetween(any(), any());
    verify(alertRepository, never()).save(any());
  }

  private Budget mockBudget(
      UUID budgetId, String scope, UUID scopeId, BigDecimal amount, int windowDays) {
    Budget budget = mock(Budget.class);

    lenient().when(budget.getBudgetId()).thenReturn(budgetId);
    lenient().when(budget.getUserId()).thenReturn(userId);
    lenient().when(budget.getScope()).thenReturn(scope);
    lenient().when(budget.getScopeId()).thenReturn(scopeId);
    lenient().when(budget.getAmount()).thenReturn(amount);
    lenient().when(budget.getWindowDays()).thenReturn(windowDays);
    lenient().when(budget.isEnabled()).thenReturn(true);

    return budget;
  }
}
