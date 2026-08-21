package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.service.optimization.rule.model.OptimizationRule;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleEngine {

  private final RuleSet ruleSet;

  public RuleEngine(RuleSet ruleSet) {
    this.ruleSet = ruleSet;
  }

  public List<OptimizationRule> loadActiveRules(List<OptimizationRule> allRules) {
    return ruleSet.loadActiveRules(allRules);
  }
}
