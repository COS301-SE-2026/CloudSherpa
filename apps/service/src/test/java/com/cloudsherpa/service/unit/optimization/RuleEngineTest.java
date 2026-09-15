package com.cloudsherpa.service.unit.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.repositories.OptimizationMetricStatisticsRepository;
import com.cloudsherpa.lib.repositories.OptimizationRecommendationRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.optimization.rule.ConditionEvaluator;
import com.cloudsherpa.service.optimization.rule.RuleEngine;
import com.cloudsherpa.service.optimization.rule.RuleSet;
import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import com.cloudsherpa.service.optimization.rule.model.RecommendationCandidate;
import com.cloudsherpa.service.optimization.rule.model.StatField;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

  @Mock private RuleSet ruleSet;
  @Mock private ConditionEvaluator conditionEvaluator;
  @Mock private OptimizationMetricStatisticsRepository statisticsRepository;
  @Mock private ResourceRepository resourceRepository;
  @Mock private OptimizationRecommendationRepository recommendationRepository;

  @InjectMocks private RuleEngine ruleEngine;

  @Test
  void testEvaluateRuleInactiveRuleReturnsEmptyList() {
    OptimizationRule rule = mock(OptimizationRule.class);
    when(ruleSet.loadActiveRules(List.of(rule))).thenReturn(List.of());

    List<RecommendationCandidate> candidates = ruleEngine.evaluateRule(rule);

    assertTrue(candidates.isEmpty(), "Disabled rules must not generate recommendations");
  }

  @Test
  void testEvaluateRule_GeneratesCandidate_WhenAllConditionsMatch() {
    UUID resourceId = UUID.randomUUID();

    MetricThresholdCondition condition = mock(MetricThresholdCondition.class);
    when(condition.metricName()).thenReturn("cpu_utilization");
    when(condition.windowNumDays()).thenReturn(7);

    when(condition.field()).thenReturn(StatField.MAXIMUM);

    OptimizationRule rule = mock(OptimizationRule.class);
    when(rule.ruleId()).thenReturn("TEST-RULE");
    when(rule.actionType()).thenReturn(OptimizationActionTypeEnum.DOWNSIZE);
    when(rule.metricThresholdConditions()).thenReturn(List.of(condition));
    when(rule.providers()).thenReturn(List.of(ProviderEnum.AWS));

    when(ruleSet.loadActiveRules(List.of(rule))).thenReturn(List.of(rule));

    OptimizationMetricStatistics stat = mock(OptimizationMetricStatistics.class);
    when(stat.getResourceId()).thenReturn(resourceId);
    when(stat.getProvider()).thenReturn(ProviderEnum.AWS);

    when(stat.getMaximumValue()).thenReturn(new BigDecimal(90.0));

    when(statisticsRepository.findByMetricNameAndWindowNumDays("cpu_utilization", 7))
        .thenReturn(List.of(stat));

    when(conditionEvaluator.matches(condition, stat)).thenReturn(true);
    when(conditionEvaluator.evidenceKey(condition)).thenReturn("cpu_utilization_max_7d");

    when(recommendationRepository.findDismissedByResourceIdAndRuleId(resourceId, "TEST-RULE"))
        .thenReturn(Optional.empty());

    List<RecommendationCandidate> candidates = ruleEngine.evaluateRule(rule);

    assertEquals(1, candidates.size());
    assertEquals(resourceId, candidates.get(0).resourceId());
    assertEquals(OptimizationActionTypeEnum.DOWNSIZE, candidates.get(0).actionType());
    assertTrue(candidates.get(0).evidence().containsKey("cpu_utilization_max_7d"));
  }
}
