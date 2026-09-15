package com.cloudsherpa.service.unit.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.service.optimization.rule.ConditionEvaluator;
import com.cloudsherpa.service.optimization.rule.model.ComparisonOperator;
import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.StatField;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConditionEvaluatorTest {

  private ConditionEvaluator evaluator;

  @BeforeEach
  void setUp() {
    evaluator = new ConditionEvaluator();
  }

  @Test
  void testMatchesGreaterThanOperatorReturnsTrue() {
    MetricThresholdCondition condition = mock(MetricThresholdCondition.class);

    when(condition.field()).thenReturn(StatField.MAXIMUM);
    when(condition.operator()).thenReturn(ComparisonOperator.GREATER_THAN);
    when(condition.threshold()).thenReturn(new BigDecimal(80.0));

    OptimizationMetricStatistics stat = mock(OptimizationMetricStatistics.class);

    when(stat.getMaximumValue()).thenReturn(new BigDecimal(85.5));

    boolean result = evaluator.matches(condition, stat);

    assertTrue(result, "85.5 is greater than 80.0, should return true");
  }

  @Test
  void testMatchesLessThanOperatorReturnsFalse() {
    MetricThresholdCondition condition = mock(MetricThresholdCondition.class);

    when(condition.field()).thenReturn(StatField.P95);
    when(condition.operator()).thenReturn(ComparisonOperator.LESS_THAN);
    when(condition.threshold()).thenReturn(new BigDecimal(10.0));

    OptimizationMetricStatistics stat = mock(OptimizationMetricStatistics.class);

    when(stat.getP95Value()).thenReturn(new BigDecimal(15.0));

    boolean result = evaluator.matches(condition, stat);

    assertFalse(result, "15.0 is not less than 10.0, should return false");
  }

  @Test
  void testMatchesNullStatisticValueReturnsFalse() {
    MetricThresholdCondition condition = mock(MetricThresholdCondition.class);

    when(condition.field()).thenReturn(StatField.AVERAGE);

    OptimizationMetricStatistics stat = mock(OptimizationMetricStatistics.class);

    when(stat.getAverageValue()).thenReturn(null);

    boolean result = evaluator.matches(condition, stat);

    assertFalse(result, "Null statistic value should result in a false match");
  }

  @Test
  void testEvidenceKeyGeneratesCorrectFormat() {
    MetricThresholdCondition condition = mock(MetricThresholdCondition.class);

    when(condition.metricName()).thenReturn("cpu_utilization");
    when(condition.field()).thenReturn(StatField.MAXIMUM);
    when(condition.windowNumDays()).thenReturn(7);

    String key = evaluator.evidenceKey(condition);

    assertEquals("cpu_utilization_maximum_7d", key);
  }

  @Test
  void testMatchesEqualOperatorReturnsTrue() {
    MetricThresholdCondition condition = mock(MetricThresholdCondition.class);

    when(condition.field()).thenReturn(StatField.AVERAGE);
    when(condition.operator()).thenReturn(ComparisonOperator.EQUAL);
    when(condition.threshold()).thenReturn(new BigDecimal(10.0));

    OptimizationMetricStatistics stat = mock(OptimizationMetricStatistics.class);

    when(stat.getAverageValue()).thenReturn(new BigDecimal(10.0));

    boolean result = evaluator.matches(condition, stat);

    assertTrue(result, "10.0 is equal to 10.0, should return true");
  }
}
