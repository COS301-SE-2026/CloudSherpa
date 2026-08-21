package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.lib.repositories.OptimizationMetricStatisticsRepository;
import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleEngine {

  private final RuleSet ruleSet;
  private final ConditionEvaluator conditionEvaluator;
  private final OptimizationMetricStatisticsRepository statisticsRepository;

  public RuleEngine(
      RuleSet ruleSet,
      ConditionEvaluator conditionEvaluator,
      OptimizationMetricStatisticsRepository statisticsRepository) {
    this.ruleSet = ruleSet;
    this.conditionEvaluator = conditionEvaluator;
    this.statisticsRepository = statisticsRepository;
  }

  public List<OptimizationRule> loadActiveRules(List<OptimizationRule> allRules) {
    return ruleSet.loadActiveRules(allRules);
  }

  public List<OptimizationMetricStatistics> findMatchingStatistics(
      MetricThresholdCondition condition) {

    List<OptimizationMetricStatistics> candidates =
        statisticsRepository.findByMetricNameAndWindowNumDays(
            condition.metricName(), condition.windowNumDays());

    List<OptimizationMetricStatistics> matched = new ArrayList<>();

    for (OptimizationMetricStatistics stats : candidates) {
      if (conditionEvaluator.matches(condition, stats)) {
        matched.add(stats);
      }
    }

    return matched;
  }
}
