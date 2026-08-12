package com.cloudsherpa.lib.repositories;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.entities.NormalizedMetrics;
import com.cloudsherpa.lib.projections.AggregatedMetric;

public interface NormalizedMetricsRepository extends JpaRepository<NormalizedMetrics, UUID> {
  // Spring translates this method name into:
  // SELECT * FROM normalized_metrics WHERE recorded_at BETWEEN ? AND ?
  List<NormalizedMetrics> findByRecordedAtBetween(OffsetDateTime startTime, OffsetDateTime endTime);

  List<NormalizedMetrics> findByPeriodStartBetween(
      OffsetDateTime startTime, OffsetDateTime endTime);

  @Query(
    value =
        """
        SELECT
          nm.resource_id AS resourceId,
          nm.metric_name AS metricName,
          CAST(nm.metric_type AS text) AS metricType,
          nm.metric_value AS metricValue,
          nm.unit AS unit,
          nm.period_start AS periodStart,
          nm.period_end AS periodEnd,
          1 AS sampleCount
        FROM normalized_metrics nm
        WHERE nm.period_start BETWEEN :fromDate AND :toDate
        ORDER BY nm.period_start ASC
        """,
    nativeQuery = true)
  List<AggregatedMetric> findAggregatedMetricsByPeriod(
      @Param("fromDate") OffsetDateTime fromDate,
      @Param("toDate") OffsetDateTime toDate,
      @Param("bucketWidth") String bucketWidth);

  
    @Query(
      value = 
      """
          SELECT
            nm.metric_value AS value,
            nm.period_start AS timestamp
            FROM normalized_metrics nm WHERE nm.resource_id = :resourceId AND metric_name = :metricName ORDER BY nm.period_start DESC;
          """,
        nativeQuery = true
    )
  List<TimestampedNumericDataPoint> getTimestampedMetricValues(@Param("resourceId") UUID metricId, @Param("metricName") String metricName, Pageable pageable);

  @Query(
    value = 
      """
          SELECT
            nm.metric_value AS value,
            nm.period_start AS timestamp
            FROM normalized_metrics nm WHERE nm.resource_id = :resourceId AND nm.metric_name = :metricName AND nm.period_start > :from
            ORDER BY nm.period_start ASC;
          """,
        nativeQuery = true
  )
  List<TimestampedNumericDataPoint> getTimestampedMetricValuesAfterDate(@Param("resourceId") UUID resourceId, @Param("metricName") String metricName, @Param("from") Instant from);
}