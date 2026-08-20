package com.cloudsherpa.service.optimization.rule;

import java.util.List;

public class RuleValidationException extends RuntimeException {

  private final List<String> errors;

  public RuleValidationException(String ruleId, List<String> errors) {
    super("Rule '" + ruleId + "' failed validation: " + String.join("; ", errors));
    this.errors = errors;
  }

  public List<String> getErrors() {
    return errors;
  }
}
