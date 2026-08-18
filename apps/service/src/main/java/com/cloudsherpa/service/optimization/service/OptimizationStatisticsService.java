package com.cloudsherpa.service.optimization.service;

import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.lib.projections.OptimizationStatisticsAggregate;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.lib.repositories.OptimizationMetricStatisticsRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimizationStatisticsService {

  public static final int FOUR_DAY_WINDOW = 4;
  public static final int SEVEN_DAY_WINDOW = 7;
  public static final int THIRTY_DAY_WINDOW = 30;

  private final OptimizationMetricStatisticsRepository statisticsRepository;
  private final NormalizedMetricsRepository normalizedMetricsRepository;

  public OptimizationStatisticsService(
      OptimizationMetricStatisticsRepository statisticsRepository,
      NormalizedMetricsRepository normalizedMetricsRepository) {
    this.statisticsRepository = statisticsRepository;
    this.normalizedMetricsRepository = normalizedMetricsRepository;
  }

  @Transactional(readOnly = true)
  public List<OptimizationMetricStatistics> getFourDayStatistics() {
    return findStatisticsForWindow(FOUR_DAY_WINDOW);
  }

  @Transactional(readOnly = true)
  public List<OptimizationMetricStatistics> getSevenDayStatistics() {
    return findStatisticsForWindow(SEVEN_DAY_WINDOW);
  }

  @Transactional(readOnly = true)
  public List<OptimizationMetricStatistics> getThirtyDayStatistics() {
    return findStatisticsForWindow(THIRTY_DAY_WINDOW);
  }

  @Transactional(readOnly = true)
  public List<OptimizationMetricStatistics> getStatisticsForWindow(int windowNumDays) {
    return findStatisticsForWindow(windowNumDays);
  }

  private List<OptimizationMetricStatistics> findStatisticsForWindow(int windowNumDays) {
    validateWindow(windowNumDays);
    return statisticsRepository.findByWindowNumDays(windowNumDays);
  }

  private void validateWindow(int windowNumDays) {
    if (windowNumDays != FOUR_DAY_WINDOW
        && windowNumDays != SEVEN_DAY_WINDOW
        && windowNumDays != THIRTY_DAY_WINDOW) {
      throw new IllegalArgumentException(
          "Statistics are only supported for 4, 7, and 30-day windows");
    }
  }

  @Transactional
  public void recalculateStatistics(OffsetDateTime windowEnd, int windowNumDays) {
    validateWindow(windowNumDays);

    OffsetDateTime windowStart = windowEnd.minusDays(windowNumDays);

    List<OptimizationStatisticsAggregate> aggregates =
        normalizedMetricsRepository.aggregateStatistics(windowStart, windowEnd);

    List<OptimizationMetricStatistics> statisticsToSave = new ArrayList<>();
    for (OptimizationStatisticsAggregate aggregate : aggregates) {
      OptimizationMetricStatistics stat =
          toStatistics(aggregate, windowNumDays, windowStart, windowEnd);
      statisticsToSave.add(stat);
    }

    statisticsRepository.saveAll(statisticsToSave);
  }

  private OptimizationMetricStatistics toStatistics(
      OptimizationStatisticsAggregate aggregate,
      int windowNumDays,
      OffsetDateTime windowStart,
      OffsetDateTime windowEnd) {

    UUID statisticsId;

    Optional<OptimizationMetricStatistics> existingStat =
        statisticsRepository
            .findByResourceIdAndMetricNameAndWindowNumDaysAndWindowStartAndWindowEnd(
                aggregate.getResourceId(),
                aggregate.getMetricName(),
                windowNumDays,
                windowStart,
                windowEnd);

    if (existingStat.isPresent()) {
      statisticsId = existingStat.get().getStatisticsId();
    } else {
      statisticsId = UUID.randomUUID();
    }

    return OptimizationMetricStatistics.builder()
        .statisticsId(statisticsId)
        .resourceId(aggregate.getResourceId())
        .provider(aggregate.getProvider())
        .metricName(aggregate.getMetricName())
        .windowNumDays(windowNumDays)
        .minimumValue(aggregate.getMinimumValue())
        .maximumValue(aggregate.getMaximumValue())
        .averageValue(aggregate.getAverageValue())
        .medianValue(aggregate.getMedianValue())
        .p95Value(aggregate.getP95Value())
        .p99Value(aggregate.getP99Value())
        .standardDeviation(aggregate.getStandardDeviation())
        .spikeCount(0)
        .peakDurationSeconds(0)
        .windowStart(windowStart)
        .windowEnd(windowEnd)
        .calculatedAt(OffsetDateTime.now(ZoneOffset.UTC))
        .build();
  }
}
