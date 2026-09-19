package com.cloudsherpa.service.alerts.service;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.Budget;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.sse.SseService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BudgetEvaluationService {
  private static final String ALERT_STATUS_ACTIVE = "ACTIVE";
  private static final String ALERT_TYPE_BUDGET = "BUDGET";

  private final NormalizedCostsRepository normalizedCostsRepository;
  private final AlertRepository alertRepository;
  private final SseService sseService;

  public BudgetEvaluationService(
      NormalizedCostsRepository normalizedCostsRepository,
      AlertRepository alertRepository,
      SseService sseService) {
    this.normalizedCostsRepository = normalizedCostsRepository;
    this.alertRepository = alertRepository;
    this.sseService = sseService;
  }

  public void evaluateCurrentSpend(Budget budget) {
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

  private BigDecimal sumScopedCost(Budget budget, OffsetDateTime from, OffsetDateTime to) {
    return switch (budget.getScope()) {
      case "RESOURCE" -> normalizedCostsRepository.sumTotalCostBetweenForResourceId(
          budget.getScopeId().toString(), from, to);
      case "ACCOUNT" -> normalizedCostsRepository.sumTotalCostBetweenForBillingAccountId(
          budget.getScopeId().toString(), from, to);
      default -> normalizedCostsRepository.sumTotalCostBetween(from, to);
    };
  }

  private void upsertAlert(Budget budget, BigDecimal value) {
    String canonicalKey = buildCanonicalKey(budget);

    Optional<Alert> existing =
        alertRepository.findByCanonicalKeyAndStatus(canonicalKey, ALERT_STATUS_ACTIVE);

    Alert alert;
    if (existing.isPresent()) {
      alert = existing.get();
      alert.setPayload(buildPayload(budget, value));
      alert.setLastSeen(OffsetDateTime.now(ZoneOffset.UTC));
    } else {
      alert = buildNewAlert(budget, value, canonicalKey);
    }

    alertRepository.save(alert);
    sseService.broadcast(budget.getUserId(), "alert", alert);
  }

  private Alert buildNewAlert(Budget budget, BigDecimal value, String canonicalKey) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    return Alert.builder()
        .userId(budget.getUserId())
        .widgetId(null)
        .alertType(ALERT_TYPE_BUDGET)
        .severity("WARNING")
        .title(buildTitle(budget))
        .message(buildMessage(budget, value))
        .payload(buildPayload(budget, value))
        .status(ALERT_STATUS_ACTIVE)
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
