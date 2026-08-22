package com.cloudsherpa.service.optimization.rule;

import com.cloudsherpa.service.optimization.rule.model.RecommendationCandidate;
// import com.cloudsherpa.service.optimization.rule.model.RecommendationResult
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConflictResolver {

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

  public void resolveAndPersist(
      List<RecommendationCandidate> draftCandidates, UUID userId, OffsetDateTime windowEnd) {
    // Implement next
  }
}
