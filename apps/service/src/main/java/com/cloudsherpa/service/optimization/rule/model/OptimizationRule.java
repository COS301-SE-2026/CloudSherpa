package com.cloudsherpa.service.optimization.rule.model;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.lib.entities.ProviderEnum;
import java.util.List;

public record OptimizationRule(
    String ruleId,
    boolean enabled,
    OptimizationActionTypeEnum actionType,
    List<ProviderEnum> providers,
    List<String> resourceTypes,
    List<MetricThresholdCondition> metricThresholdConditions,
    boolean requireNotProtected,
    String requiredSupportedAction) {}
