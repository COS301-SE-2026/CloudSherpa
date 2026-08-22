package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.service.metrics.MetricDisplayNameMapper;
import com.cloudsherpa.service.optimization.rule.model.ComparisonOperator;
import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import com.cloudsherpa.service.optimization.rule.model.StatField;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleCatalog {

  private static final List<String> COMPUTE_RESOURCE_TYPES =
      List.of("ec2_instance", "gce_instance");

  public List<OptimizationRule> getAllRules() {
    return List.of(computeDownsizeRule(), computeTerminateIdleRule());
  }

  // Recommends downsizing compute instances whose P95 CPU stayed below 10% over the last 4 days.
  private OptimizationRule computeDownsizeRule() {
    MetricThresholdCondition lowCpu =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.CPU_UTILIZATION,
            4,
            StatField.P95,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(10));

    return new OptimizationRule(
        "COMPUTE-DOWNSIZE",
        true,
        OptimizationActionTypeEnum.DOWNSIZE,
        null,
        COMPUTE_RESOURCE_TYPES,
        List.of(lowCpu),
        true,
        null);
  }

  // Recommends terminating compute instances with near-zero CPU and network activity over 7 days.
  private OptimizationRule computeTerminateIdleRule() {
    MetricThresholdCondition idleCpu =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.CPU_UTILIZATION,
            7,
            StatField.MAXIMUM,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(5));

    MetricThresholdCondition idleNetworkIn =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.NETWORK_IN,
            7,
            StatField.MAXIMUM,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(1000));

    return new OptimizationRule(
        "COMPUTE-TERMINATE-IDLE",
        true,
        OptimizationActionTypeEnum.TERMINATE,
        null,
        COMPUTE_RESOURCE_TYPES,
        List.of(idleCpu, idleNetworkIn),
        true,
        null);
  }
}
