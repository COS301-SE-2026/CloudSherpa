package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.lib.entities.OptimizationRecommendation;
import com.cloudsherpa.lib.entities.OptimizationStatusEnum;
import com.cloudsherpa.lib.repositories.OptimizationRecommendationRepository;
import com.cloudsherpa.service.optimization.rule.model.RecommendationCandidate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConflictResolver {

  private final OptimizationRecommendationRepository recommendationRepository;

  public ConflictResolver(OptimizationRecommendationRepository recommendationRepository) {
    this.recommendationRepository = recommendationRepository;
  }

  // Group candidates by resource
  public Map<UUID, List<RecommendationCandidate>> groupByResourceId(
      List<RecommendationCandidate> candidates) {
    Map<UUID, List<RecommendationCandidate>> grouped = new HashMap<>();

    for (RecommendationCandidate candidate : candidates) {
      grouped
          // used computeIfAbsent since sonar recommended using it
          // checks the map for the key candidate.resourceId()
          // if the key exists, it grabs the existing array associated with that id and returns it
          // if the key is missing, it executes the lambda function, to create a new empty array
          // and saves it undera that id, then returns the new list
          .computeIfAbsent(candidate.resourceId(), k -> new ArrayList<>())
          .add(candidate);
    }

    return grouped;
  }

  // Remove duplicate candidates from same rule per resource
  public void deduplicateByRule(List<RecommendationCandidate> candidatesForResource) {

    Map<String, RecommendationCandidate> ruleToCandidate = new HashMap<>();

    for (RecommendationCandidate candidate : new ArrayList<>(candidatesForResource)) {
      if (ruleToCandidate.containsKey(candidate.ruleId())) {
        candidatesForResource.remove(candidate);
      } else {
        ruleToCandidate.put(candidate.ruleId(), candidate);
      }
    }
  }

  // Reject candidates with missing evidence
  public List<RecommendationCandidate> validateEvidence(
      Map<UUID, List<RecommendationCandidate>> grouped) {
    List<RecommendationCandidate> validated = new ArrayList<>();

    for (List<RecommendationCandidate> candidatesPerResource : grouped.values()) {
      for (RecommendationCandidate candidate : candidatesPerResource) {
        if (candidate.evidence() != null && !candidate.evidence().isEmpty()) {
          validated.add(candidate);
        }
      }
    }

    return validated;
  }

  public List<RecommendationCandidate> rankAndSelectWinners(
      List<RecommendationCandidate> validated) {

    // Group by resource
    Map<UUID, List<RecommendationCandidate>> groupedByResource = new HashMap<>();
    for (RecommendationCandidate candidate : validated) {
      groupedByResource
          .computeIfAbsent(candidate.resourceId(), k -> new ArrayList<>())
          .add(candidate);
    }

    List<RecommendationCandidate> winners = new ArrayList<>();

    for (List<RecommendationCandidate> group : groupedByResource.values()) {
      if (group.isEmpty()) {
        continue;
      }

      // Rank by hierarchy and select winner
      RecommendationCandidate winner = rankByCandidateHierarchy(group);
      winners.add(winner);
    }

    return winners;
  }

  private Integer getHierarchyWeight(OptimizationActionTypeEnum action) {
    return switch (action) {
      case TERMINATE -> 100;
      case DOWNSIZE -> 50;
      case SUSPEND -> 25;
    };
  }

  private RecommendationCandidate rankByCandidateHierarchy(
      List<RecommendationCandidate> candidates) {

    RecommendationCandidate winner = candidates.get(0);
    int highestWeight = getHierarchyWeight(winner.actionType());

    for (int i = 1; i < candidates.size(); i++) {
      RecommendationCandidate current = candidates.get(i);

      int currentWeight = getHierarchyWeight(current.actionType());

      if (currentWeight > highestWeight) {
        winner = current;
        highestWeight = currentWeight;
      }
    }

    return winner;
  }

  private void persistWinners(List<RecommendationCandidate> winners, OffsetDateTime windowEnd) {
    for (RecommendationCandidate winner : winners) {
      // Check if a recommendation already exists for this resource and rule
      Optional<OptimizationRecommendation> existing =
          recommendationRepository.findByResourceIdAndRuleId(winner.resourceId(), winner.ruleId());

      if (existing.isPresent()) {
        OptimizationRecommendation rec = existing.get();

        if (rec.getStatus().equals(OptimizationStatusEnum.ACTIVE)) {
          continue;
        }

        // Update if it was in a different status
        rec.setStatus(OptimizationStatusEnum.ACTIVE);
        rec.setUpdatedAt(windowEnd);
        recommendationRepository.save(rec);
      } else {
        // Create new recommendation
        OptimizationRecommendation rec =
            new OptimizationRecommendation(
                winner.resourceId(),
                winner.provider(),
                winner.ruleId(),
                winner.actionType(),
                OptimizationStatusEnum.ACTIVE,
                winner.evidence(),
                windowEnd);

        recommendationRepository.save(rec);
      }
    }
  }

  public void resolveAndPersist(
      List<RecommendationCandidate> draftCandidates, OffsetDateTime windowEnd) {

    if (draftCandidates.isEmpty()) {
      return;
    }

    Map<UUID, List<RecommendationCandidate>> grouped = groupByResourceId(draftCandidates);

    for (List<RecommendationCandidate> group : grouped.values()) {
      deduplicateByRule(group);
    }

    List<RecommendationCandidate> validated = validateEvidence(grouped);

    if (validated.isEmpty()) {
      return;
    }

    List<RecommendationCandidate> winners = rankAndSelectWinners(validated);

    persistWinners(winners, windowEnd);
  }
}
