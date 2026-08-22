package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.service.optimization.rule.model.ComparisonOperator;
import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import com.cloudsherpa.service.optimization.rule.model.StatField;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleCatalog {

  public List<OptimizationRule> getAllRules() {
    return List.of(computeDownsizeRule());
  }

  // Recommends downsizing compute instances whose P95 CPU stayed below 10% over the last 4 days.
  private OptimizationRule computeDownsizeRule() {
    MetricThresholdCondition lowCpu =
        new MetricThresholdCondition(
            "CPU Utilization", 4, StatField.P95, ComparisonOperator.LESS_THAN, new BigDecimal(10));

    return new OptimizationRule(
        "COMPUTE-DOWNSIZE",
        true,
        OptimizationActionTypeEnum.DOWNSIZE,
        null,
        List.of("compute_instance", "virtual_machine"),
        List.of(lowCpu),
        true,
        null);
  }
}
