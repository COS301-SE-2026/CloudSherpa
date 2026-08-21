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

  // Finds statistics that satisfy the one condition for the rule by using ConditionEvalkuator
  public List<OptimizationMetricStatistics> findMatchingStatistics(
      OptimizationRule rule, MetricThresholdCondition condition) {

    List<OptimizationMetricStatistics> candidates =
        statisticsRepository.findByMetricNameAndWindowNumDays(
            condition.metricName(), condition.windowNumDays());

    List<OptimizationMetricStatistics> matched = new ArrayList<>();

    for (OptimizationMetricStatistics stats : candidates) {
      boolean providerMatches =
          rule.providers() == null
              || rule.providers().isEmpty()
              || rule.providers().contains(stats.getProvider());

      boolean resourceTypeMatches = matchesResourceType(rule, stats);

      if (providerMatches && resourceTypeMatches && conditionEvaluator.matches(condition, stats)) {
        matched.add(stats);
      }
    }

    return matched;
  }

  // Checks whether the statistic's resource is allowed by the rule
  // No resource types means all types are accepted
  private boolean matchesResourceType(OptimizationRule rule, OptimizationMetricStatistics stats) {

    // If no types are specified, it's an automatic match
    if (rule.resourceTypes() == null || rule.resourceTypes().isEmpty()) {
      return true;
    }

    Optional<Resource> optionalResource = resourceRepository.findById(stats.getResourceId());

    if (optionalResource.isEmpty()) {
      return false;
    }

    Resource resource = optionalResource.get();

    return rule.resourceTypes().contains(resource.getResourceType());
  }

  // Set models the mathematical Set (also prevents duplicate resources)
  // Finds the resources that matches every condition in 1 rule
  public Set<UUID> findMatchingResourceIds(OptimizationRule rule) {
    Set<UUID> matchingResourceIds = null;

    for (MetricThresholdCondition condition : rule.metricThresholdConditions()) {

      List<OptimizationMetricStatistics> statisticsList = findMatchingStatistics(rule, condition);

      Set<UUID> conditionResourceIds = new HashSet<>();
      for (OptimizationMetricStatistics stats : statisticsList) {
        conditionResourceIds.add(stats.getResourceId());
      }

      // Keep only the resources that match ALL conditions (Intersection)
      if (matchingResourceIds == null) {
        // for the first condition, initialize the matchingResourcesIds
        matchingResourceIds = conditionResourceIds;
      } else {
        // strip out anything that isn't in this new set

        // AND behaviour
        // Previous matches:  A, B
        // Current matches:   B, C
        // After retainAll:   B
        matchingResourceIds.retainAll(conditionResourceIds);
      }

      if (matchingResourceIds.isEmpty()) {
        return matchingResourceIds;
      }
    }

    if (matchingResourceIds == null) {
      return Set.of();
    }

    return matchingResourceIds;
  }

  // This is the main candidate-generation method
  // Finds statistics matching each condition.
  // Indexes them by condition and resource ID.
  // Finds resources present in every condition.
  // Builds evidence from the matching statistic values.
  // Creates one DRAFT candidate per matching resource.
  public List<RecommendationCandidate> evaluateRule(OptimizationRule rule) {
    Map<MetricThresholdCondition, Map<UUID, OptimizationMetricStatistics>>
        // Map structure: Condition -> (ResourceID -> Statistics)
        // Example:
        // CPU P95 < 20
        //    Resource A -> CPU statistics
        //    Resource B -> CPU statistics
        statisticsByCondition = new HashMap<>();

    Map<UUID, ProviderEnum> providerByResourceId = new HashMap<>();
    Set<UUID> matchingResourceIds = null;

    for (MetricThresholdCondition condition : rule.metricThresholdConditions()) {
      // Resource ID -> matching statistics row
      Map<UUID, OptimizationMetricStatistics> statisticsByResourceId = new HashMap<>();

      for (OptimizationMetricStatistics stats : findMatchingStatistics(rule, condition)) {
        statisticsByResourceId.put(stats.getResourceId(), stats);
        providerByResourceId.put(stats.getResourceId(), stats.getProvider());
      }

      statisticsByCondition.put(condition, statisticsByResourceId);
      // Condition 1 -> Resource A -> Statistics
      // Condition 1 -> Resource B -> Statistics
      // Condition 2 -> Resource B -> Statistics
      // Condition 2 -> Resource C -> Statistics

      // Keep only resources that match every condition (AND behavior)
      if (matchingResourceIds == null) {
        matchingResourceIds = new HashSet<>(statisticsByResourceId.keySet());
      } else {
        matchingResourceIds.retainAll(statisticsByResourceId.keySet());
      }

      if (matchingResourceIds.isEmpty()) {
        return List.of();
      }
    }

    if (matchingResourceIds == null) {
      return List.of();
    }

    List<RecommendationCandidate> candidates = new ArrayList<>();

    for (UUID resourceId : matchingResourceIds) {
      Map<String, Object> evidence = new HashMap<>();

      for (MetricThresholdCondition condition : rule.metricThresholdConditions()) {
        OptimizationMetricStatistics stats = statisticsByCondition.get(condition).get(resourceId);

        evidence.put(
            conditionEvaluator.evidenceKey(condition), extractEvidenceValue(condition, stats));
      }

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
}
