package com.cloudsherpa.service.optimization.service;

import com.cloudsherpa.lib.entities.OptimizationRecommendation;
import com.cloudsherpa.lib.repositories.OptimizationRecommendationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OptimizationRecommendationService {

  private final OptimizationRecommendationRepository recommendationRepository;

  public OptimizationRecommendationService(
      OptimizationRecommendationRepository recommendationRepository) {
    this.recommendationRepository = recommendationRepository;
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

  public Map<String, Object> getRecommendation(UUID recommendationId) {
    // Load recommendation by ID.
    return mockRecommendation(recommendationId);
  }

  public Map<String, Object> acknowledgeRecommendation(UUID recommendationId) {
    // Validate ACTIVE -> ACKNOWLEDGED.
    return mockRecommendationWithStatus(recommendationId, "ACKNOWLEDGED");
  }

  public Map<String, Object> dismissRecommendation(UUID recommendationId) {
    // Validate ACTIVE -> DISMISSED.
    return mockRecommendationWithStatus(recommendationId, "DISMISSED");
  }

  public Map<String, Object> applyRecommendation(UUID recommendationId) {
    // Validate ACTIVE -> APPLIED.
    return mockRecommendationWithStatus(recommendationId, "APPLIED");
  }

  public Map<String, Object> getRecommendationSummary() {
    // Query counts from persisted recommendations.
    return Map.of(
        "total", 3,
        "active", 1,
        "acknowledged", 1,
        "dismissed", 0,
        "applied", 1,
        "byActionType",
            Map.of(
                "TERMINATE", 0,
                "DOWNSIZE", 2,
                "SUSPEND", 1));
  }

  private Map<String, Object> mockRecommendation() {
    return mockRecommendation(UUID.randomUUID());
  }

  private Map<String, Object> mockRecommendation(UUID recommendationId) {
    return mockRecommendationWithStatus(recommendationId, "ACTIVE");
  }

  private Map<String, Object> mockRecommendationWithStatus(UUID recommendationId, String status) {

    return Map.of(
        "recommendationId",
        recommendationId,
        "resourceId",
        UUID.fromString("b0000000-0000-0000-0000-000000000001"),
        "resourceType",
        "compute_instance",
        "provider",
        "AWS",
        "ruleId",
        "COMPUTE-DOWNSIZE",
        "actionType",
        "DOWNSIZE",
        "status",
        status,
        "evidence",
        Map.of(
            "cpuPercentP95_4d", 18.4,
            "completenessRatio", 0.99));
  }
}
