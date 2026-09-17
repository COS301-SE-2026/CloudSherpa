package com.cloudsherpa.service.unit.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.OptimizationActionTypeEnum;
import com.cloudsherpa.lib.entities.OptimizationRecommendation;
import com.cloudsherpa.lib.entities.OptimizationStatusEnum;
import com.cloudsherpa.lib.repositories.OptimizationRecommendationRepository;
import com.cloudsherpa.service.optimization.rule.ConflictResolver;
import com.cloudsherpa.service.optimization.rule.model.RecommendationCandidate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConflictResolverTest {

  @Mock private OptimizationRecommendationRepository repository;

  @InjectMocks private ConflictResolver resolver;

  @Test
  void testRankAndSelectWinnersHierarchyEnforced() {
    UUID resourceId = UUID.randomUUID();

    RecommendationCandidate suspendCandidate = mock(RecommendationCandidate.class);

    when(suspendCandidate.resourceId()).thenReturn(resourceId);
    when(suspendCandidate.actionType()).thenReturn(OptimizationActionTypeEnum.SUSPEND);

    RecommendationCandidate terminateCandidate = mock(RecommendationCandidate.class);

    when(terminateCandidate.resourceId()).thenReturn(resourceId);
    when(terminateCandidate.actionType()).thenReturn(OptimizationActionTypeEnum.TERMINATE);

    RecommendationCandidate downsizeCandidate = mock(RecommendationCandidate.class);

    when(downsizeCandidate.resourceId()).thenReturn(resourceId);
    when(downsizeCandidate.actionType()).thenReturn(OptimizationActionTypeEnum.DOWNSIZE);

    List<RecommendationCandidate> validated =
        List.of(suspendCandidate, downsizeCandidate, terminateCandidate);

    List<RecommendationCandidate> winners = resolver.rankAndSelectWinners(validated);

    assertEquals(1, winners.size(), "Only one winner per resource should be selected");
    assertEquals(
        OptimizationActionTypeEnum.TERMINATE,
        winners.get(0).actionType(),
        "TERMINATE (weight 100) should win over DOWNSIZE (50) and SUSPEND (25)");
  }

  @Test
  void testDeduplicateByRuleRemovesDuplicates() {
    RecommendationCandidate candidate1 = mock(RecommendationCandidate.class);

    when(candidate1.ruleId()).thenReturn("RULE-A");

    RecommendationCandidate candidate2 = mock(RecommendationCandidate.class);

    when(candidate2.ruleId()).thenReturn("RULE-A");

    List<RecommendationCandidate> candidates = new ArrayList<>(List.of(candidate1, candidate2));

    resolver.deduplicateByRule(candidates);

    assertEquals(1, candidates.size(), "Duplicate candidate for the same rule should be removed");
  }

  @Test
  void testValidateEvidenceRejectsMissingEvidence() {
    UUID resourceId = UUID.randomUUID();

    RecommendationCandidate validCandidate = mock(RecommendationCandidate.class);

    when(validCandidate.evidence()).thenReturn(Map.of("cpu_max_4d", new BigDecimal(1.0)));

    RecommendationCandidate invalidCandidate = mock(RecommendationCandidate.class);

    when(invalidCandidate.evidence()).thenReturn(Map.of());

    Map<UUID, List<RecommendationCandidate>> grouped =
        Map.of(resourceId, List.of(validCandidate, invalidCandidate));

    List<RecommendationCandidate> validated = resolver.validateEvidence(grouped);

    assertEquals(1, validated.size(), "Candidate without evidence should be filtered out");
  }

  @Test
  void testResolveAndPersistSupersedesOldRules() {
    UUID resourceId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    RecommendationCandidate winner = mock(RecommendationCandidate.class);

    when(winner.resourceId()).thenReturn(resourceId);
    when(winner.ruleId()).thenReturn("NEW-WINNER-RULE");
    when(winner.actionType()).thenReturn(OptimizationActionTypeEnum.TERMINATE);
    when(winner.evidence()).thenReturn(Map.of("key", "val"));

    OptimizationRecommendation oldActiveRec = new OptimizationRecommendation();
    oldActiveRec.setRuleId("OLD-LOSER-RULE");
    oldActiveRec.setStatus(OptimizationStatusEnum.ACTIVE);

    when(repository.findByResourceIdAndRuleId(resourceId, "NEW-WINNER-RULE"))
        .thenReturn(Optional.empty());

    when(repository.findActiveByResourceId(resourceId)).thenReturn(List.of(oldActiveRec));

    resolver.resolveAndPersist(List.of(winner), now);

    verify(repository, times(2)).save(any(OptimizationRecommendation.class));
    assertEquals(OptimizationStatusEnum.SUPERSEDED, oldActiveRec.getStatus());
  }
}
