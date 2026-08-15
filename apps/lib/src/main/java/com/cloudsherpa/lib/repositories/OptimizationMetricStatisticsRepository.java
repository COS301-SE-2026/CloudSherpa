package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.OptimizationMetricStatistics;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptimizationMetricStatisticsRepository extends JpaRepository<OptimizationMetricStatistics, UUID> {

  List<OptimizationMetricStatistics> findByResourceId(UUID resourceId);

  List<OptimizationMetricStatistics> findByResourceIdAndMetricName(UUID resourceId, String metricName);

  List<OptimizationMetricStatistics> findByWindowNumDays(Integer windowNumDays);

  List<OptimizationMetricStatistics> findByWindowStartAndWindowEnd(OffsetDateTime windowStart, OffsetDateTime windowEnd);
}