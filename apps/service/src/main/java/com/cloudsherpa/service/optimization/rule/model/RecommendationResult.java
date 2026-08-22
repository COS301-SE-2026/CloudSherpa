package com.cloudsherpa.service.optimization.rule.model;

import com.cloudsherpa.lib.entities.OptimizationStatusEnum;
import java.util.Map;
import java.util.UUID;

// Represents a resolved recommendation ready for persistence
public record RecommendationResult(
    UUID resourceId,
    String provider,
    String ruleId,
    String actionType,
    OptimizationStatusEnum status, // ACTIVE, SUPERSEDED
    Map<String, Object> evidence,
    String resolutionReason, // Why this candidate won/lost
    Integer hierarchyRank) {

  public static RecommendationResult active(
      RecommendationCandidate candidate, String reason, Integer rank) {
    return new RecommendationResult(
        candidate.resourceId(),
        candidate.provider().name(),
        candidate.ruleId(),
        candidate.actionType().name(),
        OptimizationStatusEnum.ACTIVE,
        candidate.evidence(),
        reason,
        rank);
  }

  public static RecommendationResult superseded(RecommendationCandidate candidate, String reason) {
    return new RecommendationResult(
        candidate.resourceId(),
        candidate.provider().name(),
        candidate.ruleId(),
        candidate.actionType().name(),
        OptimizationStatusEnum.SUPERSEDED,
        candidate.evidence(),
        reason,
        null);
  }
}
