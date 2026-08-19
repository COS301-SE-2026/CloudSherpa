package com.cloudsherpa.service.optimization.worker;

import com.cloudsherpa.lib.entities.ProcessingWatermark;
import com.cloudsherpa.lib.repositories.ProcessingWatermarkRepository;
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

  public TenantOptimizationWorker(
      ProcessingWatermarkRepository watermarkRepository,
      OptimizationStatisticsService statisticsService) {
    this.watermarkRepository = watermarkRepository;
    this.statisticsService = statisticsService;
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
    // Evaluate rules.
    // Resolve recommendation conflicts.
    // Persist recommendations.

    statisticsService.recalculateStatistics(windowEnd, 4);
    statisticsService.recalculateStatistics(windowEnd, 7);
    statisticsService.recalculateStatistics(windowEnd, 30);

    watermark.setLastProcessedPeriod(windowEnd);
    watermark.setLastSuccessfulRun(windowEnd);
    watermark.setUpdatedAt(windowEnd);

    watermarkRepository.save(watermark);
  }
}
