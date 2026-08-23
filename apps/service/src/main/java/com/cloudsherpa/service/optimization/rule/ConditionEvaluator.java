package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.service.optimization.rule.model.ComparisonOperator;
import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.StatField;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ConditionEvaluator {

  public boolean matches(MetricThresholdCondition condition, OptimizationMetricStatistics stat) {
    BigDecimal actual = extractField(condition.field(), stat);
    if (actual == null) {
      return false;
    }

    return compare(actual, condition.operator(), condition.threshold());
  }

  public String evidenceKey(MetricThresholdCondition condition) {
    return condition.metricName()
        + "_"
        + condition.field().name().toLowerCase()
        + "_"
        + condition.windowNumDays()
        + "d";
  }

  private BigDecimal extractField(StatField field, OptimizationMetricStatistics stat) {
    return switch (field) {
      case MINIMUM -> stat.getMinimumValue();
      case MAXIMUM -> stat.getMaximumValue();
      case AVERAGE -> stat.getAverageValue();
      case MEDIAN -> stat.getMedianValue();
      case P95 -> stat.getP95Value();
      case P99 -> stat.getP99Value();
      case STANDARD_DEVIATION -> stat.getStandardDeviation();
    };
  }

  private boolean compare(BigDecimal actual, ComparisonOperator operator, BigDecimal threshold) {
    int comparison = actual.compareTo(threshold);

    // BigDecimal compareTo() method returns an int
    // negative if actual is < threshold
    // zero if actual = threshold
    // positive if actual > threshold
    return switch (operator) {
      case LESS_THAN -> comparison < 0;
      case LESS_THAN_OR_EQUAL -> comparison <= 0;
      case GREATER_THAN -> comparison > 0;
      case GREATER_THAN_OR_EQUAL -> comparison >= 0;
      case EQUAL -> comparison == 0;
      case NOT_EQUAL -> comparison != 0;
    };
  }
}
