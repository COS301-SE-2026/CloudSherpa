package com.cloudsherpa.service.unit.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.service.optimization.rule.RuleValidationException;
import com.cloudsherpa.service.optimization.rule.RuleValidator;
import com.cloudsherpa.service.optimization.rule.model.ComparisonOperator;
import com.cloudsherpa.service.optimization.rule.model.MetricThresholdCondition;
import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import com.cloudsherpa.service.optimization.rule.model.StatField;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RuleValidatorTest {

  private RuleValidator validator;

  @BeforeEach
  void setUp() {
    validator = new RuleValidator();
  }

  @Test
  void testValidateValidRuleReturnsEmptyErrors() {
    OptimizationRule rule = mock(OptimizationRule.class);

    when(rule.ruleId()).thenReturn("COMPUTE-DOWNSIZE");
    when(rule.actionType()).thenReturn(OptimizationActionTypeEnum.DOWNSIZE);

    MetricThresholdCondition condition = mock(MetricThresholdCondition.class);

    when(condition.metricName()).thenReturn("cpu");
    when(condition.windowNumDays()).thenReturn(7);
    when(condition.field()).thenReturn(StatField.P95);
    when(condition.operator()).thenReturn(ComparisonOperator.LESS_THAN);
    when(condition.threshold()).thenReturn(new BigDecimal("10"));

    when(rule.metricThresholdConditions()).thenReturn(List.of(condition));

    List<String> errors = validator.validate(rule);

    assertTrue(errors.isEmpty(), "Valid rule should have zero errors");
  }

  @Test
  void testValidateOrThrow_MissingRuleId_ThrowsException() {
    OptimizationRule rule = mock(OptimizationRule.class);
    when(rule.ruleId()).thenReturn("");

    RuleValidationException exception =
        assertThrows(RuleValidationException.class, () -> validator.validateOrThrow(rule));

    assertTrue(exception.getErrors().contains("ruleId must not be blank"));
  }

  @Test
  void testValidate_InvalidWindowNumDays_ReturnsError() {
    OptimizationRule rule = mock(OptimizationRule.class);

    when(rule.ruleId()).thenReturn("TEST-RULE");
    when(rule.actionType()).thenReturn(OptimizationActionTypeEnum.TERMINATE);

    MetricThresholdCondition condition = mock(MetricThresholdCondition.class);

    when(condition.metricName()).thenReturn("cpu");
    when(condition.windowNumDays()).thenReturn(14);
    when(condition.field()).thenReturn(StatField.MAXIMUM);
    when(condition.operator()).thenReturn(ComparisonOperator.EQUAL);
    when(condition.threshold()).thenReturn(new BigDecimal("0"));

    when(rule.metricThresholdConditions()).thenReturn(List.of(condition));

    List<String> errors = validator.validate(rule);

    assertEquals(1, errors.size());
    assertTrue(errors.contains("metricThresholdConditions.windowNumDays must be 4, 7, or 30"));
  }
}
