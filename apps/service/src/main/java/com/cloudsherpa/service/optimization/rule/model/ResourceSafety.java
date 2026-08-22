package com.cloudsherpa.service.optimization.rule.model;

import java.util.Set;
import java.util.UUID;

// Encapsulates all safety checks for a resource
public record ResourceSafety(
    UUID resourceId,
    boolean isProtected, // Has "protected" tag
    boolean canTerminate, // Policy allows TERMINATE
    boolean canDownsize, // Policy allows DOWNSIZE
    boolean canSuspend, // Policy allows SUSPEND
    Set<String> tags // All resource tags for context
    ) {

  public boolean isSafeFor(String actionType) {
    return switch (actionType) {
      case "TERMINATE" -> canTerminate;
      case "DOWNSIZE" -> canDownsize;
      case "SUSPEND" -> canSuspend;
      default -> false;
    };
  }
}
