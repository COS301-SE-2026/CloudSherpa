package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleSet {

  private final RuleValidator ruleValidator;

  public RuleSet(RuleValidator ruleValidator) {
    this.ruleValidator = ruleValidator;
  }

  public List<OptimizationRule> loadActiveRules(List<OptimizationRule> allRules) {
    List<String> seenRuleIds = new ArrayList<>();
    List<OptimizationRule> activeRules = new ArrayList<>();

    for (OptimizationRule rule : allRules) {
      ruleValidator.validateOrThrow(rule);

      if (seenRuleIds.contains(rule.ruleId())) {
        throw new RuleValidationException(rule.ruleId(), List.of("duplicate ruleId"));
      }
      seenRuleIds.add(rule.ruleId());

      if (rule.enabled()) {
        activeRules.add(rule);
      }
    }

    return activeRules;
  }
}
