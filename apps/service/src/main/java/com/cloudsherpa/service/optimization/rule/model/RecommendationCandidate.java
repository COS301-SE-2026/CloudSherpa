package com.cloudsherpa.service.optimization.rule.model;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.lib.entities.OptimizationStatusEnum;
import com.cloudsherpa.lib.entities.ProviderEnum;
import java.util.Map;
import java.util.UUID;

public record RecommendationCandidate(
    UUID resourceId,
    ProviderEnum provider,
    String ruleId,
    OptimizationActionTypeEnum actionType,
    OptimizationStatusEnum status,
    Map<String, Object> evidence) {

  public static RecommendationCandidate draft(
      UUID resourceId,
      ProviderEnum provider,
      String ruleId,
      OptimizationActionTypeEnum actionType,
      Map<String, Object> evidence) {
    return new RecommendationCandidate(
        resourceId, provider, ruleId, actionType, OptimizationStatusEnum.DRAFT, evidence);
  }
}
