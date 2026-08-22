package com.cloudsherpa.service.optimization.worker;

import com.cloudsherpa.lib.entities.ProcessingWatermark;
import com.cloudsherpa.lib.repositories.ProcessingWatermarkRepository;
// import com.cloudsherpa.service.optimization.rule.RuleCatalog
// import com.cloudsherpa.service.optimization.rule.RuleEngine
import com.cloudsherpa.service.optimization.service.OptimizationStatisticsService;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TenantOptimizationWorker {

  private static final String PIPELINE_NAME = "optimization";

  private final ProcessingWatermarkRepository watermarkRepository;
  private final OptimizationStatisticsService statisticsService;

  // private final RuleCatalog ruleCatalog
  // private final RuleEngine ruleEngine

  public TenantOptimizationWorker(
      ProcessingWatermarkRepository watermarkRepository,
      OptimizationStatisticsService statisticsService) {
    this.watermarkRepository = watermarkRepository;
    this.statisticsService = statisticsService;
    // this.ruleCatalog = ruleCatalog
    // this.ruleEngine = ruleEngine
  }

  @Transactional
  public void executeDatabaseWork(UUID userId) {
    OffsetDateTime windowEnd = OffsetDateTime.now(ZoneOffset.UTC);

    Optional<ProcessingWatermark> dbResult =
        watermarkRepository.findByUserIdAndPipelineName(userId, PIPELINE_NAME);

    ProcessingWatermark watermark;
    if (dbResult.isPresent()) {
      watermark = dbResult.get();
    } else {
      watermark = new ProcessingWatermark(userId, PIPELINE_NAME, null, null, windowEnd);
    }

    // Calculate 4-day statistics.
    // Calculate 7-day statistics.
    // Calculate 30-day statistics.
    statisticsService.recalculateStatistics(windowEnd, 4);
    statisticsService.recalculateStatistics(windowEnd, 7);
    statisticsService.recalculateStatistics(windowEnd, 30);

    // Evaluate rules: Load all rules from the catalog, validate them, and run each rule
    // against the newly calculated statistics to generate draft recommendation candidates.
    // List<OptimizationRule> allRules = ruleCatalog.getAllRules()
    // List<OptimizationRule> activeRules = ruleEngine.loadActiveRules(allRules)

    // List<RecommendationCandidate> draftCandidates = new ArrayList<>()
    // for OptimizationRule rule : activeRules{ (for loop)
    //   List<RecommendationCandidate> ruleCandidates = ruleEngine.evaluateRule(rule)
    //   draftCandidates.addAll(ruleCandidates)
    //

    // Resolve recommendation conflicts.
    // Apply the conflict resolution hierarchy (TERMINATE > MODERNIZE > DOWNSIZE > SUSPEND).
    // Group candidates by resource_id, deduplicate, check safety policies, and select winners.
    // Promote winning candidates from DRAFT -> ACTIVE status.
    // Mark non-winners as SUPERSEDED or DISMISSED.

    // Persist recommendations.
    // Save finalized recommendations to the optimization_recommendation table.

    watermark.setLastProcessedPeriod(windowEnd);
    watermark.setLastSuccessfulRun(windowEnd);
    watermark.setUpdatedAt(windowEnd);

    watermarkRepository.save(watermark);
  }
}
