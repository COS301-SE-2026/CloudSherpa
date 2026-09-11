package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.lib.entities.ProviderEnum;
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
      List.of("AWS/EC2", "gce_instance", "Microsoft.Compute/virtualMachines");

  public List<OptimizationRule> getAllRules() {
    return List.of(
        computeDownsizeRule(),
        computeTerminateIdleRule(),
        computeSuspendIdleRule(),
        computeDownsizeMemoryRule(),
        computeSuspendLowMemoryAndCpuRule(),
        computeSuspendLowNetworkRule(),
        computeSuspendLowDiskBytesRule(),
        computeDownsizeStorageTierRule());
  }

  // ! ---------------------------------------- TERMINATE ----------------------------------------
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
        List.of(idleCpu, idleNetworkIn));
  }

  // ! ---------------------------------------- TERMINATE ----------------------------------------

  // # ---------------------------------------- DOWNSIZE ----------------------------------------
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
        List.of(lowCpu));
  }

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
        List.of(ProviderEnum.AWS),
        COMPUTE_RESOURCE_TYPES,
        List.of(lowMemory));
  }

  private OptimizationRule computeDownsizeStorageTierRule() {
    MetricThresholdCondition lowPctDiskUsed30d =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.PERCENTAGE_DISK_SPACE_USED,
            4,
            StatField.P95,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(20));

    return new OptimizationRule(
        "COMPUTE-DOWNSIZE-STORAGE-TIER",
        true,
        OptimizationActionTypeEnum.DOWNSIZE,
        List.of(ProviderEnum.AWS),
        COMPUTE_RESOURCE_TYPES,
        List.of(lowPctDiskUsed30d));
  }

  // # ---------------------------------------- DOWNSIZE ----------------------------------------

  // ? ---------------------------------------- SUSPEND ----------------------------------------
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
            new BigDecimal(1000000));

    return new OptimizationRule(
        "COMPUTE-SUSPEND-IDLE",
        true,
        OptimizationActionTypeEnum.SUSPEND,
        null,
        COMPUTE_RESOURCE_TYPES,
        List.of(lowCpu, lowNetworkIn));
  }

  private OptimizationRule computeSuspendLowMemoryAndCpuRule() {
    MetricThresholdCondition lowCpuP95 =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.CPU_UTILIZATION,
            4,
            StatField.P95,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(15));

    MetricThresholdCondition lowMemoryP95 =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.MEMORY_UTILIZATION,
            4,
            StatField.P95,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(25));

    return new OptimizationRule(
        "COMPUTE-SUSPEND-LOW-CPU-MEMORY",
        true,
        OptimizationActionTypeEnum.SUSPEND,
        null,
        COMPUTE_RESOURCE_TYPES,
        List.of(lowCpuP95, lowMemoryP95));
  }

  private OptimizationRule computeSuspendLowNetworkRule() {
    MetricThresholdCondition lowNetworkIn =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.NETWORK_IN,
            4,
            StatField.MAXIMUM,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(5000000)); // 5 000 000 bytes = 5 MB

    MetricThresholdCondition lowNetworkOut =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.NETWORK_OUT,
            4,
            StatField.MAXIMUM,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(5000000)); // 5 000 000 bytes = 5 MB

    return new OptimizationRule(
        "COMPUTE-SUSPEND-LOW-NETWORK",
        true,
        OptimizationActionTypeEnum.SUSPEND,
        null,
        COMPUTE_RESOURCE_TYPES,
        List.of(lowNetworkIn, lowNetworkOut));
  }

  private OptimizationRule computeSuspendLowDiskBytesRule() {
    MetricThresholdCondition lowDiskRead =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.DISK_READ_BYTES,
            4,
            StatField.MAXIMUM,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(5000000)); // < 5000000 bytes = 5 MB

    MetricThresholdCondition lowDiskWrite =
        new MetricThresholdCondition(
            MetricDisplayNameMapper.DISK_WRITE_BYTES,
            4,
            StatField.MAXIMUM,
            ComparisonOperator.LESS_THAN,
            new BigDecimal(5000000)); // < 5000000 bytes = 5 MB

    return new OptimizationRule(
        "COMPUTE-SUSPEND-LOW-DISK-BYTES",
        true,
        OptimizationActionTypeEnum.SUSPEND,
        null,
        COMPUTE_RESOURCE_TYPES,
        List.of(lowDiskRead, lowDiskWrite));
  }
  // ? ---------------------------------------- SUSPEND ----------------------------------------
}
