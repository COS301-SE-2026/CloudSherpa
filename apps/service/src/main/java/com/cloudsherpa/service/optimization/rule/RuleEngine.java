package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.OptimizationMetricStatisticsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import com.cloudsherpa.service.optimization.rule.model.RecommendationCandidate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RuleEngine {

  private final RuleSet ruleSet;
  private final ConditionEvaluator conditionEvaluator;
  private final OptimizationMetricStatisticsRepository statisticsRepository;
  private final ResourceRepository resourceRepository;

  public RuleEngine(
      RuleSet ruleSet,
      ConditionEvaluator conditionEvaluator,
      OptimizationMetricStatisticsRepository statisticsRepository,
      ResourceRepository resourceRepository) {
    this.ruleSet = ruleSet;
    this.conditionEvaluator = conditionEvaluator;
    this.statisticsRepository = statisticsRepository;
    this.resourceRepository = resourceRepository;
  }

  // Delegates to the ruleset
  // Validates each rule, returns valid enabled rules
  public List<OptimizationRule> loadActiveRules(List<OptimizationRule> allRules) {
    return ruleSet.loadActiveRules(allRules);
  }

  // Finds statistics that satisfy the one condition for the rule by using ConditionEvaluator
  public List<OptimizationMetricStatistics> findMatchingStatistics(
      OptimizationRule rule, MetricThresholdCondition condition) {

    Map<UUID, OptimizationMetricStatistics> latestStatisticsByResource =
        findLatestStatistics(rule, condition);

    List<OptimizationMetricStatistics> matchedStatistics = new ArrayList<>();

    // The threshold is evaluated only after the latest row has been selected.
    for (OptimizationMetricStatistics stats : latestStatisticsByResource.values()) {
      if (conditionEvaluator.matches(condition, stats)) {
        matchedStatistics.add(stats);
      }
    }

    return matchedStatistics;
  }

  // Loads eligible rows and keeps only the newest row for each resource.
  private Map<UUID, OptimizationMetricStatistics> findLatestStatistics(
      OptimizationRule rule, MetricThresholdCondition condition) {

    List<OptimizationMetricStatistics> candidates =
        statisticsRepository.findByMetricNameAndWindowNumDays(
            condition.metricName(), condition.windowNumDays());

    Map<UUID, OptimizationMetricStatistics> latestStatisticsByResource = new HashMap<>();

    for (OptimizationMetricStatistics stats : candidates) {
      if (!matchesRuleScope(rule, stats)) {
        continue;
      }

      UUID resourceId = stats.getResourceId();
      OptimizationMetricStatistics existing = latestStatisticsByResource.get(resourceId);

      if (existing == null || isMoreRecent(stats, existing)) {
        latestStatisticsByResource.put(resourceId, stats);
      }
    }

    return latestStatisticsByResource;
  }

  // Provider and resource type filters decide whether a row belongs to the rule.
  private boolean matchesRuleScope(OptimizationRule rule, OptimizationMetricStatistics stats) {

    return matchesProvider(rule, stats) && matchesResourceType(rule, stats);
  }

  // An empty provider filter means that every provider is allowed.
  private boolean matchesProvider(OptimizationRule rule, OptimizationMetricStatistics stats) {
    return rule.providers() == null
        || rule.providers().isEmpty()
        || rule.providers().contains(stats.getProvider());
  }

  // An empty resource type filter means that every resource type is allowed.
  private boolean matchesResourceType(OptimizationRule rule, OptimizationMetricStatistics stats) {
    if (rule.resourceTypes() == null || rule.resourceTypes().isEmpty()) {
      return true;
    }

    Optional<Resource> resource = resourceRepository.findById(stats.getResourceId());

    return resource
        .map(Resource::getResourceType)
        .filter(rule.resourceTypes()::contains)
        .isPresent();
  }

  // Evaluates one rule and creates draft candidates for matching resources.
  public List<RecommendationCandidate> evaluateRule(OptimizationRule rule) {
    // Invalid or disabled rules must not generate recommendations.
    if (!isActiveRule(rule)) {
      return List.of();
    }

    List<Map<UUID, OptimizationMetricStatistics>> statisticsByCondition = new ArrayList<>();

    Map<UUID, ProviderEnum> providerByResourceId = new HashMap<>();

    // Each condition is evaluated independently before results are combined.
    for (MetricThresholdCondition condition : rule.metricThresholdConditions()) {
      Map<UUID, OptimizationMetricStatistics> matchingStatistics =
          toStatisticsByResource(findMatchingStatistics(rule, condition));

      statisticsByCondition.add(matchingStatistics);
      addProviders(providerByResourceId, matchingStatistics);
    }

    Set<UUID> matchingResourceIds = findResourcesMatchingEveryCondition(statisticsByCondition);

    if (matchingResourceIds.isEmpty()) {
      return List.of();
    }

    return createCandidates(rule, matchingResourceIds, statisticsByCondition, providerByResourceId);
  }

  // RuleSet validates the rule and returns it only when it is enabled.
  private boolean isActiveRule(OptimizationRule rule) {
    return !ruleSet.loadActiveRules(List.of(rule)).isEmpty();
  }

  // Converts matching rows into quick resource ID lookups.
  private Map<UUID, OptimizationMetricStatistics> toStatisticsByResource(
      List<OptimizationMetricStatistics> statistics) {

    Map<UUID, OptimizationMetricStatistics> statisticsByResource = new HashMap<>();

    for (OptimizationMetricStatistics stat : statistics) {
      statisticsByResource.put(stat.getResourceId(), stat);
    }

    return statisticsByResource;
  }

  // Stores the provider needed when the final candidate is created.
  private void addProviders(
      Map<UUID, ProviderEnum> providerByResourceId,
      Map<UUID, OptimizationMetricStatistics> statisticsByResource) {

    for (OptimizationMetricStatistics stats : statisticsByResource.values()) {
      providerByResourceId.putIfAbsent(stats.getResourceId(), stats.getProvider());
    }
  }

  // Intersects condition results so every condition must pass.
  private Set<UUID> findResourcesMatchingEveryCondition(
      List<Map<UUID, OptimizationMetricStatistics>> statisticsByCondition) {

    if (statisticsByCondition.isEmpty()) {
      return Set.of();
    }

    Set<UUID> matchingResourceIds = new HashSet<>(statisticsByCondition.get(0).keySet());

    for (int conditionIndex = 1; conditionIndex < statisticsByCondition.size(); conditionIndex++) {

      matchingResourceIds.retainAll(statisticsByCondition.get(conditionIndex).keySet());

      if (matchingResourceIds.isEmpty()) {
        return Set.of();
      }
    }

    return matchingResourceIds;
  }

  // Builds one DRAFT candidate for each resource that passed every condition.
  private List<RecommendationCandidate> createCandidates(
      OptimizationRule rule,
      Set<UUID> matchingResourceIds,
      List<Map<UUID, OptimizationMetricStatistics>> statisticsByCondition,
      Map<UUID, ProviderEnum> providerByResourceId) {

    List<RecommendationCandidate> candidates = new ArrayList<>();

    for (UUID resourceId : matchingResourceIds) {
      Map<String, Object> evidence = createEvidence(rule, resourceId, statisticsByCondition);

      candidates.add(
          RecommendationCandidate.draft(
              resourceId,
              providerByResourceId.get(resourceId),
              rule.ruleId(),
              rule.actionType(),
              evidence));
    }

    return candidates;
  }

  // Collects the actual values that caused each condition to pass.
  private Map<String, Object> createEvidence(
      OptimizationRule rule,
      UUID resourceId,
      List<Map<UUID, OptimizationMetricStatistics>> statisticsByCondition) {

    Map<String, Object> evidence = new HashMap<>();

    for (int conditionIndex = 0;
        conditionIndex < rule.metricThresholdConditions().size();
        conditionIndex++) {

      MetricThresholdCondition condition = rule.metricThresholdConditions().get(conditionIndex);

      OptimizationMetricStatistics stats =
          statisticsByCondition.get(conditionIndex).get(resourceId);

      evidence.put(
          conditionEvaluator.evidenceKey(condition), extractEvidenceValue(condition, stats));
    }

    return evidence;
  }

  // Selects the statistic field referenced by the condition.
  private BigDecimal extractEvidenceValue(
      MetricThresholdCondition condition, OptimizationMetricStatistics stats) {

    return switch (condition.field()) {
      case MINIMUM -> stats.getMinimumValue();
      case MAXIMUM -> stats.getMaximumValue();
      case AVERAGE -> stats.getAverageValue();
      case MEDIAN -> stats.getMedianValue();
      case P95 -> stats.getP95Value();
      case P99 -> stats.getP99Value();
      case STANDARD_DEVIATION -> stats.getStandardDeviation();
    };
  }

  // Compares calculation timestamps to choose the newest row.
  private boolean isMoreRecent(
      OptimizationMetricStatistics candidate, OptimizationMetricStatistics existing) {

    if (candidate.getCalculatedAt() == null) {
      return false;
    }

    if (existing.getCalculatedAt() == null) {
      return true;
    }

    return candidate.getCalculatedAt().isAfter(existing.getCalculatedAt());
  }
}
