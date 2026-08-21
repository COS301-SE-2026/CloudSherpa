package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.OptimizationMetricStatisticsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

  public List<OptimizationRule> loadActiveRules(List<OptimizationRule> allRules) {
    return ruleSet.loadActiveRules(allRules);
  }

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
}
