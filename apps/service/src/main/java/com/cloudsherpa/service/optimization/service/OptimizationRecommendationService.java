package com.cloudsherpa.service.optimization.service;

import com.cloudsherpa.lib.entities.OptimizationRecommendation;
import com.cloudsherpa.lib.entities.OptimizationStatusEnum;
import com.cloudsherpa.lib.entities.RecommendationHistory;
import com.cloudsherpa.lib.repositories.OptimizationRecommendationRepository;
import com.cloudsherpa.lib.repositories.RecommendationHistoryRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OptimizationRecommendationService {

  private final OptimizationRecommendationRepository recommendationRepository;
  private final RecommendationHistoryRepository historyRepository;

  public OptimizationRecommendationService(
      OptimizationRecommendationRepository recommendationRepository,
      RecommendationHistoryRepository historyRepository) {
    this.recommendationRepository = recommendationRepository;
    this.historyRepository = historyRepository;
  }

  // Query all recommendations from database
  public List<Map<String, Object>> getRecommendations() {

    List<OptimizationRecommendation> recommendations = recommendationRepository.findAll();

    List<Map<String, Object>> resultList = new ArrayList<>(recommendations.size());

    for (OptimizationRecommendation rec : recommendations) {
      Map<String, Object> map = toMap(rec);
      resultList.add(map);
    }

    return resultList;
  }

  public Map<String, Object> getRecommendation(UUID recommendationId) {
    OptimizationRecommendation recommendation =
        recommendationRepository.findById(recommendationId).orElse(null);

    if (recommendation == null) {
      return Map.of("error", "Recommendation not found");
    }

    return toMap(recommendation);
  }

  public Map<String, Object> acknowledgeRecommendation(UUID recommendationId) {
    return updateRecommendationStatus(recommendationId, OptimizationStatusEnum.ACKNOWLEDGED);
  }

  public Map<String, Object> dismissRecommendation(UUID recommendationId) {
    return updateRecommendationStatus(recommendationId, OptimizationStatusEnum.DISMISSED);
  }

  public Map<String, Object> applyRecommendation(UUID recommendationId) {
    return updateRecommendationStatus(recommendationId, OptimizationStatusEnum.APPLIED);
  }

  public Map<String, Object> getRecommendationSummary() {
    List<OptimizationRecommendation> allRecommendations = recommendationRepository.findAll();

    int activeCount = 0;
    int acknowledgedCount = 0;
    int dismissedCount = 0;
    int appliedCount = 0;

    int terminateCount = 0;
    int downsizeCount = 0;
    int suspendCount = 0;

    for (OptimizationRecommendation rec : allRecommendations) {

      OptimizationStatusEnum status = rec.getStatus();

      if (status == OptimizationStatusEnum.ACTIVE) {
        activeCount++;
      } else if (status == OptimizationStatusEnum.ACKNOWLEDGED) {
        acknowledgedCount++;
      } else if (status == OptimizationStatusEnum.DISMISSED) {
        dismissedCount++;
      } else if (status == OptimizationStatusEnum.APPLIED) {
        appliedCount++;
      }

      String actionName = rec.getActionType().name();

      if ("TERMINATE".equals(actionName)) {
        terminateCount++;
      } else if ("DOWNSIZE".equals(actionName)) {
        downsizeCount++;
      } else if ("SUSPEND".equals(actionName)) {
        suspendCount++;
      }
    }

    Map<String, Object> actionTypeMap = new HashMap<>();
    actionTypeMap.put("TERMINATE", terminateCount);
    actionTypeMap.put("DOWNSIZE", downsizeCount);
    actionTypeMap.put("SUSPEND", suspendCount);

    Map<String, Object> summary = new HashMap<>();
    summary.put("total", allRecommendations.size());
    summary.put("active", activeCount);
    summary.put("acknowledged", acknowledgedCount);
    summary.put("dismissed", dismissedCount);
    summary.put("applied", appliedCount);
    summary.put("actionType", actionTypeMap);

    return summary;
  }

  // Helpers
  private Map<String, Object> toMap(OptimizationRecommendation rec) {
    return Map.of(
        "recommendationId",
        rec.getRecommendationId(),
        "resourceId",
        rec.getResourceId(),
        "provider",
        rec.getProvider().name(),
        "ruleId",
        rec.getRuleId(),
        "actionType",
        rec.getActionType().name(),
        "status",
        rec.getStatus().name(),
        "evidence",
        rec.getEvidence() != null ? rec.getEvidence() : Map.of(),
        "createdAt",
        rec.getCreatedAt(),
        "updatedAt",
        rec.getUpdatedAt());
  }

  private Map<String, Object> updateRecommendationStatus(
      UUID recommendationId, OptimizationStatusEnum newStatus) {
    OptimizationRecommendation recommendation =
        recommendationRepository.findById(recommendationId).orElse(null);

    if (recommendation == null) {
      return Map.of("error", "Recommendation not found");
    }

    OptimizationStatusEnum previousStatus = recommendation.getStatus();
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    recommendation.setStatus(newStatus);
    recommendation.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

    recommendationRepository.save(recommendation);

    logRecommendationStatusChange(recommendation, previousStatus, newStatus, now);

    return toMap(recommendation);
  }

  private void logRecommendationStatusChange(
      OptimizationRecommendation recommendation,
      OptimizationStatusEnum previousStatus,
      OptimizationStatusEnum newStatus,
      OffsetDateTime changedAt) {

    RecommendationHistory history =
        new RecommendationHistory(
            recommendation.getRecommendationId(),
            recommendation.getResourceId(),
            recommendation.getProvider(),
            recommendation.getRuleId(),
            recommendation.getActionType(),
            previousStatus,
            newStatus,
            recommendation.getEvidence(),
            changedAt);

    historyRepository.save(history);
  }
}
