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
    return List.of(
        computeDownsizeRule(),
        computeTerminateIdleRule(),
        computeSuspendIdleRule(),
        computeDownsizeMemoryRule());
  }

  // Recommends downsizing compute instances whose P95 CPU utilization stayed below 10% over the
  // last 4 days.
  // P95 filters out temporary spikes, capturing only sustained low usage patterns.
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

  // Recommends terminating compute instances that are completely idle: near-zero CPU and near-zero
  // network activity over 4 days.
  // Requires BOTH conditions to be met to avoid false positives.
  // Uses MAXIMUM stat to catch instances that never even briefly spike in usage.
  // Termination is the most aggressive action, reserved for resources clearly no longer needed.
  private OptimizationRule computeTerminateIdleRule() {
    MetricThresholdCondition idleCpu =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.CPU_UTILIZATION,
            4,
            StatField.MAXIMUM,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(5));

    MetricThresholdCondition idleNetworkIn =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.NETWORK_IN,
            4,
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

  // Recommends suspending compute instances with low CPU and network over 4 days.
  // More conservative than terminate.
  // SUSPEND allows the instance to be stopped/started rather than permanently removed.
  // Criteria: P95 CPU <15% and max network <2KB indicates minimal active usage patterns.
  private OptimizationRule computeSuspendIdleRule() {
    MetricThresholdCondition lowCpu =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.CPU_UTILIZATION,
            4,
            StatField.P95,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(15));

    MetricThresholdCondition lowNetworkIn =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.NETWORK_IN,
            4,
            StatField.MAXIMUM,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(2000));

    return new OptimizationRule(
        "COMPUTE-SUSPEND-IDLE",
        true,
        OptimizationActionTypeEnum.SUSPEND,
        null,
        COMPUTE_RESOURCE_TYPES,
        List.of(lowCpu, lowNetworkIn),
        true,
        null);
  }

  // Recommends downsizing compute instances whose P95 memory utilization stayed below 20% over the
  // last 4 days.
  // This complements CPU-based downsize rules by catching instances that may have ample CPU but
  // waste memory allocation.
  private OptimizationRule computeDownsizeMemoryRule() {
    MetricThresholdCondition lowMemory =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.MEMORY_UTILIZATION,
            4,
            StatField.P95,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(20));

    return new OptimizationRule(
        "COMPUTE-DOWNSIZE-MEMORY",
        true,
        OptimizationActionTypeEnum.DOWNSIZE,
        null,
        COMPUTE_RESOURCE_TYPES,
        List.of(lowMemory),
        true,
        null);
  }
}
