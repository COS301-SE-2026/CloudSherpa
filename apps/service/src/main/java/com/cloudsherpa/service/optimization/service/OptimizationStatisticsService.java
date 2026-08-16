package com.cloudsherpa.service.optimization.service;

import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import com.cloudsherpa.lib.repositories.OptimizationMetricStatisticsRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimizationStatisticsService {

  public static final int FOUR_DAY_WINDOW = 4;
  public static final int SEVEN_DAY_WINDOW = 7;
  public static final int THIRTY_DAY_WINDOW = 30;

  private final OptimizationMetricStatisticsRepository statisticsRepository;

  public OptimizationStatisticsService(
      OptimizationMetricStatisticsRepository statisticsRepository) {
    this.statisticsRepository = statisticsRepository;
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
}
