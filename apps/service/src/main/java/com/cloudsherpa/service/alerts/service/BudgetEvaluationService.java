package com.cloudsherpa.service.alerts.service;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.AlertSeverityEnum;
import com.cloudsherpa.lib.entities.AlertStatusEnum;
import com.cloudsherpa.lib.entities.AlertTypeEnum;
import com.cloudsherpa.lib.entities.Budget;
import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.lib.repositories.BudgetRepository;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.alerts.dto.BudgetScopeContext;
import com.cloudsherpa.service.sse.SseService;
import com.cloudsherpa.service.webhooks.events.alert.budget.BudgetAlertPayload;
import com.cloudsherpa.service.webhooks.producers.WebhookProducerService;
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
  private final WebhookProducerService producerService;
  private final CloudAccountRepository cloudAccountRepository;
  private static final Logger logger = LoggerFactory.getLogger(BudgetEvaluationService.class);

  private static final String RESOURCE_SCOPE = "RESOURCE";
  private static final String ACCOUNT_SCOPE = "ACCOUNT";
  private static final String TENANT_SCOPE = "TENANT";
  private static final String UNKNOWN = "Unknown";

  public BudgetEvaluationService(
      NormalizedCostsRepository normalizedCostsRepository,
      AlertRepository alertRepository,
      SseService sseService,
      BudgetRepository budgetRepository,
      ResourceRepository resourceRepository,
      WebhookProducerService producerService,
      CloudAccountRepository cloudAccountRepository) {
    this.normalizedCostsRepository = normalizedCostsRepository;
    this.alertRepository = alertRepository;
    this.sseService = sseService;
    this.budgetRepository = budgetRepository;
    this.resourceRepository = resourceRepository;
    this.producerService = producerService;
    this.cloudAccountRepository = cloudAccountRepository;
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

    matchingBudgets.addAll(
        budgetRepository.findByUserIdAndScopeAndEnabledTrue(userId, TENANT_SCOPE));

    matchingBudgets.addAll(
        budgetRepository.findByScopeAndScopeIdAndEnabledTrue(ACCOUNT_SCOPE, accountId));

    for (Resource resource : resourceRepository.findByAccountId(accountId)) {
      matchingBudgets.addAll(
          budgetRepository.findByScopeAndScopeIdAndEnabledTrue(RESOURCE_SCOPE, resource.getId()));
    }

    for (Budget budget : matchingBudgets) {
      evaluateCurrentSpend(budget);
    }
  }

  private BigDecimal sumScopedCost(Budget budget, OffsetDateTime from, OffsetDateTime to) {
    return switch (budget.getScope()) {
      case RESOURCE_SCOPE -> sumCostForResourceBudget(budget, from, to);
      case ACCOUNT_SCOPE -> normalizedCostsRepository.sumTotalCostBetweenForAccountId(
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

    BudgetScopeContext scopeContext = resolveScopeContext(budget);

    Alert alert;
    if (existing.isPresent()) {
      alert = existing.get();
      alert.setTitle(buildTitle(scopeContext));
      alert.setMessage(buildMessage(budget, value, scopeContext));
      alert.setPayload(buildPayload(budget, value, scopeContext));
      alert.setLastSeen(OffsetDateTime.now(ZoneOffset.UTC));
    } else {
      alert = buildNewAlert(budget, value, canonicalKey, scopeContext);
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
    UUID cloudAccountId = getCloudAccountIdForWebhookEvent(budget);
    BudgetAlertPayload payload = buildWebhookEventPayload(budget, alert, value);
    producerService.produceEvent(budget.getUserId(), cloudAccountId, "alert.budget", payload);
  }

  private BudgetScopeContext resolveScopeContext(Budget budget) {
    return switch (budget.getScope()) {
      case RESOURCE_SCOPE -> resolveResourceScopeContext(budget.getScopeId());
      case ACCOUNT_SCOPE -> resolveAccountScopeContext(budget.getScopeId());
      default -> tenantScopeContext();
    };
  }

  private BudgetScopeContext resolveResourceScopeContext(UUID scopeId) {
    if (scopeId == null) {
      return new BudgetScopeContext(RESOURCE_SCOPE, "Unknown resource", UNKNOWN);
    }

    return resourceRepository
        .findById(scopeId)
        .map(
            resource ->
                new BudgetScopeContext(
                    RESOURCE_SCOPE, resource.getResourceName(), resolveProvider(resource)))
        .orElseGet(() -> new BudgetScopeContext(RESOURCE_SCOPE, scopeId.toString(), UNKNOWN));
  }

  private BudgetScopeContext resolveAccountScopeContext(UUID scopeId) {
    if (scopeId == null) {
      return new BudgetScopeContext(ACCOUNT_SCOPE, "Unknown account", UNKNOWN);
    }

    return cloudAccountRepository
        .findById(scopeId)
        .map(
            account ->
                new BudgetScopeContext(
                    ACCOUNT_SCOPE, resolveAccountName(account), resolveProvider(account)))
        .orElseGet(() -> new BudgetScopeContext(ACCOUNT_SCOPE, scopeId.toString(), UNKNOWN));
  }

  private BudgetScopeContext tenantScopeContext() {
    return new BudgetScopeContext("Multi-cloud", "All connected resources", "MULTI");
  }

  private String resolveProvider(Resource resource) {
    if (resource.getAccount() == null || resource.getAccount().getConnection() == null) {
      return "UNKNOWN";
    }

    return resource.getAccount().getConnection().getProvider().name();
  }

  private String resolveProvider(CloudAccount account) {
    if (account.getConnection() == null || account.getConnection().getProvider() == null) {
      return "UNKNOWN";
    }

    return account.getConnection().getProvider().name();
  }

  private String resolveAccountName(CloudAccount account) {
    if (account.getDisplayName() == null || account.getDisplayName().isBlank()) {
      return account.getId().toString();
    }

    return account.getDisplayName();
  }

  private Alert buildNewAlert(
      Budget budget, BigDecimal value, String canonicalKey, BudgetScopeContext scopeContext) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    return Alert.builder()
        .userId(budget.getUserId())
        .widgetId(null)
        .alertType(AlertTypeEnum.BUDGET)
        .severity(AlertSeverityEnum.WARNING)
        .title(buildTitle(scopeContext))
        .message(buildMessage(budget, value, scopeContext))
        .payload(buildPayload(budget, value, scopeContext))
        .status(AlertStatusEnum.ACTIVE)
        .canonicalKey(canonicalKey)
        .createdAt(now)
        .lastSeen(now)
        .build();
  }

  private Map<String, Object> buildPayload(
      Budget budget, BigDecimal value, BudgetScopeContext scopeContext) {
    Map<String, Object> payload = new HashMap<>();

    payload.put("budget_id", budget.getBudgetId());
    payload.put("budget_scope", budget.getScope());
    payload.put("scope_id", budget.getScopeId());
    payload.put("scope_name", scopeContext.scopeName());
    payload.put("provider", scopeContext.provider());
    payload.put("window_days", budget.getWindowDays());
    payload.put("budget_amount", budget.getAmount());
    payload.put("current_total", value);
    payload.put("overage_amount", value.subtract(budget.getAmount()));

    return payload;
  }

  private String buildCanonicalKey(Budget budget) {
    return "budget:" + budget.getBudgetId();
  }

  private String buildTitle(BudgetScopeContext scopeContext) {
    return "Budget exceeded: " + scopeContext.scopeLabel() + " spend alert";
  }

  private String buildMessage(Budget budget, BigDecimal value, BudgetScopeContext scopeContext) {
    return "Spend "
        + value
        + " exceeded the configured budget "
        + budget.getAmount()
        + " over the last "
        + budget.getWindowDays()
        + " days for "
        + scopeContext.scopeName()
        + " ["
        + scopeContext.provider()
        + "].";
  }

  private BudgetAlertPayload buildWebhookEventPayload(
      Budget budget, Alert alert, BigDecimal value) {
    return new BudgetAlertPayload(
        alert.getTitle(),
        alert.getMessage(),
        budget.getScope(),
        alert.getSeverity().toString(),
        budget.getAmount(),
        value,
        budget.getWindowDays());
  }

  private UUID getCloudAccountIdForWebhookEvent(Budget budget) {
    return switch (budget.getScope()) {
      case RESOURCE_SCOPE -> getResourceCloudAccount(budget.getScopeId());
      case ACCOUNT_SCOPE -> budget.getScopeId();
      default -> null;
    };
  }

  private UUID getResourceCloudAccount(UUID resourceId) {
    Resource resource = resourceRepository.findById(resourceId).orElse(null);

    if (resource == null) {
      return null;
    }

    return resource.getAccountId();
  }
}
