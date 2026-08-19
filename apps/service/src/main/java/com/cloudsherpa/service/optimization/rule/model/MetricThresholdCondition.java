package com.cloudsherpa.service.optimization.rule.model;

import java.math.BigDecimal;

public record MetricThresholdCondition(
    String metricName,
    int windowNumDays,
    StatField field,
    ComparisonOperator operator,
    BigDecimal threshold) {}
