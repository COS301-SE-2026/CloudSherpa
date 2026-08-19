package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleValidator {

  public List<String> validate(OptimizationRule rule) {
    List<String> errors = new ArrayList<>();

    validateRuleId(rule, errors);
    validateActionType(rule, errors);
    validateMetricThresholdConditions(rule, errors);
    validateProviders(rule, errors);
    validateResourceTypes(rule, errors);
    validateRequiredSupportedAction(rule, errors);

    return errors;
  }

  private void validateRuleId(OptimizationRule rule, List<String> errors) {
    if (rule.ruleId() == null || rule.ruleId().isBlank()) {
      errors.add("ruleId must not be blank");
    }
  }

  private void validateActionType(OptimizationRule rule, List<String> errors) {
    if (rule.actionType() == null) {
      errors.add("actionType is required");
    }
  }

  private void validateMetricThresholdConditions(OptimizationRule rule, List<String> errors) {
    if (rule.metricThresholdConditions() == null || rule.metricThresholdConditions().isEmpty()) {
      errors.add("at least one metric threshold condition is required");
      return;
    }

    for (MetricThresholdCondition condition : rule.metricThresholdConditions()) {
      validateCondition(condition, errors);
    }
  }

  private void validateProviders(OptimizationRule rule, List<String> errors) {
    if (rule.providers() != null && rule.providers().contains(null)) {
      errors.add("providers must not contain null entries");
    }
  }

  private void validateResourceTypes(OptimizationRule rule, List<String> errors) {
    if (rule.resourceTypes() == null) {
      return;
    }

    boolean hasBlankEntry = false;
    for (String resourceType : rule.resourceTypes()) {
      if (resourceType == null || resourceType.isBlank()) {
        hasBlankEntry = true;
        break;
      }
    }

    if (hasBlankEntry) {
      errors.add("resourceTypes must not contain blank entries");
    }
  }

  private void validateRequiredSupportedAction(OptimizationRule rule, List<String> errors) {
    if (rule.requiredSupportedAction() != null && rule.requiredSupportedAction().isBlank()) {
      errors.add("requiredSupportedAction must not be blank when present");
    }
  }

  private void validateCondition(MetricThresholdCondition condition, List<String> errors) {
    if (condition.metricName() == null || condition.metricName().isBlank()) {
      errors.add("metricThresholdConditions.metricName must not be blank");
    }

    if (condition.windowNumDays() != 4
        && condition.windowNumDays() != 7
        && condition.windowNumDays() != 30) {
      errors.add("metricThresholdConditions.windowNumDays must be 4, 7, or 30");
    }

    if (condition.field() == null) {
      errors.add("metricThresholdConditions.field is required");
    }

    if (condition.operator() == null) {
      errors.add("metricThresholdConditions.operator is required");
    }

    if (condition.threshold() == null) {
      errors.add("metricThresholdConditions.threshold is required");
    }
  }

  public void validateOrThrow(OptimizationRule rule) {
    List<String> errors = validate(rule);
    if (!errors.isEmpty()) {
      throw new RuleValidationException(rule.ruleId(), errors);
    }
  }
}
