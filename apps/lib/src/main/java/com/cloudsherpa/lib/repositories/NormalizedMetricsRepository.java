package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.NormalizedMetrics;
import com.cloudsherpa.lib.projections.AggregatedMetric;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NormalizedMetricsRepository extends JpaRepository<NormalizedMetrics, UUID> {
  // Spring translates this method name into:
  // SELECT * FROM normalized_metrics WHERE recorded_at BETWEEN ? AND ?
  List<NormalizedMetrics> findByRecordedAtBetween(OffsetDateTime startTime, OffsetDateTime endTime);

  List<NormalizedMetrics> findByPeriodStartBetween(
      OffsetDateTime startTime, OffsetDateTime endTime);

  @Query(
      value =
          """
          WITH bucketed_metrics AS (
            SELECT
              nm.resource_id,
              nm.metric_name,
              CAST(nm.metric_type AS text) AS metric_type,
              nm.metric_value,
              nm.unit,
              time_bucket(CAST(:bucketWidth AS INTERVAL), nm.period_start) AS bucket_start
            FROM normalized_metrics nm
            WHERE nm.period_start BETWEEN :fromDate AND :toDate
          )
          SELECT
            bm.resource_id AS resourceId,
            bm.metric_name AS metricName,
            bm.metric_type AS metricType,
            AVG(bm.metric_value) AS metricValue,
            MIN(bm.unit) AS unit,
            bm.bucket_start AS periodStart,
            bm.bucket_start + CAST(:bucketWidth AS INTERVAL) AS periodEnd,
            COUNT(*) AS sampleCount
          FROM bucketed_metrics bm
          GROUP BY
            bm.resource_id,
            bm.metric_name,
            bm.metric_type,
            bm.bucket_start
          ORDER BY bm.bucket_start ASC
          """,
      nativeQuery = true)
  List<AggregatedMetric> findAggregatedMetricsByPeriod(
      @Param("fromDate") OffsetDateTime fromDate,
      @Param("toDate") OffsetDateTime toDate,
      @Param("bucketWidth") String bucketWidth);
}