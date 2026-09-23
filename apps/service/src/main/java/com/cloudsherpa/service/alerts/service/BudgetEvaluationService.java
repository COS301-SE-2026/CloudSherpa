package com.cloudsherpa.service.alerts.service;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.AlertSeverityEnum;
import com.cloudsherpa.lib.entities.AlertStatusEnum;
import com.cloudsherpa.lib.entities.AlertTypeEnum;
import com.cloudsherpa.lib.entities.Budget;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.lib.repositories.BudgetRepository;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.sse.SseService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BudgetEvaluationService {

  private final NormalizedCostsRepository normalizedCostsRepository;
  private final AlertRepository alertRepository;
  private final SseService sseService;
  private final BudgetRepository budgetRepository;
  private final ResourceRepository resourceRepository;
  private static final Logger logger = LoggerFactory.getLogger(BudgetEvaluationService.class);

  public BudgetEvaluationService(
      NormalizedCostsRepository normalizedCostsRepository,
      AlertRepository alertRepository,
      SseService sseService,
      BudgetRepository budgetRepository,
      ResourceRepository resourceRepository) {
    this.normalizedCostsRepository = normalizedCostsRepository;
    this.alertRepository = alertRepository;
    this.sseService = sseService;
    this.budgetRepository = budgetRepository;
    this.resourceRepository = resourceRepository;
  }

  private void evaluateCurrentSpend(Budget budget) {
    if (!budget.isEnabled()) {
      return;
    }

    OffsetDateTime windowEnd = OffsetDateTime.now(ZoneOffset.UTC);
    OffsetDateTime windowStart = windowEnd.minusDays(budget.getWindowDays());
    BigDecimal currentTotal = sumScopedCost(budget, windowStart, windowEnd);

    if (currentTotal.compareTo(budget.getAmount()) < 0) {
      return;
    }

    upsertAlert(budget, currentTotal);
  }

  public void evaluateForAccount(UUID userId, UUID accountId) {
    List<Budget> matchingBudgets = new ArrayList<>();

    matchingBudgets.addAll(budgetRepository.findByUserIdAndScopeAndEnabledTrue(userId, "TENANT"));

    matchingBudgets.addAll(
        budgetRepository.findByScopeAndScopeIdAndEnabledTrue("ACCOUNT", accountId));

    for (Resource resource : resourceRepository.findByAccountId(accountId)) {
      matchingBudgets.addAll(
          budgetRepository.findByScopeAndScopeIdAndEnabledTrue("RESOURCE", resource.getId()));
    }

    for (Budget budget : matchingBudgets) {
      evaluateCurrentSpend(budget);
    }
  }

  private BigDecimal sumScopedCost(Budget budget, OffsetDateTime from, OffsetDateTime to) {
    return switch (budget.getScope()) {
      case "RESOURCE" -> sumCostForResourceBudget(budget, from, to);
      case "ACCOUNT" -> normalizedCostsRepository.sumTotalCostBetweenForAccountId(
          budget.getScopeId(), from, to);
      default -> normalizedCostsRepository.sumTotalCostBetween(from, to);
    };
  }

  private BigDecimal sumCostForResourceBudget(
      Budget budget, OffsetDateTime from, OffsetDateTime to) {
    Optional<Resource> resource = resourceRepository.findById(budget.getScopeId());

    if (resource.isEmpty()) {
      return BigDecimal.ZERO;
    }

    return normalizedCostsRepository.sumTotalCostBetweenForResourceIdentifier(
        resource.get().getResourceIdentifier(), from, to);
  }

  private void upsertAlert(Budget budget, BigDecimal value) {
    String canonicalKey = buildCanonicalKey(budget);

    Optional<Alert> existing =
        alertRepository.findByCanonicalKeyAndStatus(canonicalKey, AlertStatusEnum.ACTIVE);

    if (existing.isEmpty()
        && alertRepository
            .findByCanonicalKeyAndStatus(canonicalKey, AlertStatusEnum.DISABLED)
            .isPresent()) {
      // User disabled alerts for this budget; don't recreate one.
      return;
    }

    Alert alert;
    if (existing.isPresent()) {
      alert = existing.get();
      alert.setPayload(buildPayload(budget, value));
      alert.setLastSeen(OffsetDateTime.now(ZoneOffset.UTC));
    } else {
      alert = buildNewAlert(budget, value, canonicalKey);
    }

    alertRepository.save(alert);
    logger.info(
        "BUDGET BREACH DETECTED: budgetId={} scope={} amount={} currentSpend={}",
        budget.getBudgetId(),
        budget.getScope(),
        budget.getAmount(),
        value);
    sseService.broadcast(budget.getUserId(), "alert", alert);

    // Build & submit budget webhook event alert.budget

  }

  private Alert buildNewAlert(Budget budget, BigDecimal value, String canonicalKey) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    return Alert.builder()
        .userId(budget.getUserId())
        .widgetId(null)
        .alertType(AlertTypeEnum.BUDGET)
        .severity(AlertSeverityEnum.WARNING)
        .title(buildTitle(budget))
        .message(buildMessage(budget, value))
        .payload(buildPayload(budget, value))
        .status(AlertStatusEnum.ACTIVE)
        .canonicalKey(canonicalKey)
        .createdAt(now)
        .lastSeen(now)
        .build();
  }

  private Map<String, Object> buildPayload(Budget budget, BigDecimal value) {
    Map<String, Object> payload = new HashMap<>();

    payload.put("budget_id", budget.getBudgetId());
    payload.put("budget_amount", budget.getAmount());
    payload.put("current_total", value);

    return payload;
  }

  private String buildCanonicalKey(Budget budget) {
    return "budget:" + budget.getBudgetId();
  }

  private String buildTitle(Budget budget) {
    return "Current spend exceeded budget of: " + budget.getAmount();
  }

  private String buildMessage(Budget budget, BigDecimal value) {
    return "Current spend " + value + " has reached budget amount " + budget.getAmount();
  }
}
