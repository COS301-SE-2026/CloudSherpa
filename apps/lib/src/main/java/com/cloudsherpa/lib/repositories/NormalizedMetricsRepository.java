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
          SELECT
            -- Metadata so the frontend knows what this data is for
            nm.resource_id AS resourceId,
            nm.metric_name AS metricName,
            
            -- Cast the metric_type to a standard text string
            nm.metric_type::text AS metricType,

            -- Take all the raw metric values that fall into this specific time bucket and calculate their mean average
            AVG(nm.metric_value) AS metricValue,
            
            -- Because 'unit' is not in our GROUP BY clause, SQL forces us to aggregate it.
            -- MIN() simply grabs the first string it finds without doing any real math
            MIN(nm.unit) AS unit,
            
            -- TimescaleDB function that acts like a 'floor' function for time
            -- It rounds the raw 'period_start' down to the nearest interval of :bucketWidth
            -- (e.g., If bucketWidth is '5 minutes', 10:04:33 becomes 10:00:00)
            time_bucket(CAST(:bucketWidth AS INTERVAL), nm.period_start) AS periodStart,
            
            -- Recalculates the start time, then adds the :bucketWidth back onto it
            -- (e.g., 10:00:00 + '5 minutes' = 10:05:00).
            time_bucket(CAST(:bucketWidth AS INTERVAL), nm.period_start)
              + CAST(:bucketWidth AS INTERVAL) AS periodEnd,
            
          FROM normalized_metrics nm
          
          -- Only process rows that fall within the user's selected dashboard time range
          WHERE nm.period_start BETWEEN :fromDate AND :toDate
          
          -- Group the data into discrete piles. To be in the same pile, rows must have the exact same:
          -- Resource ID, Metric Name, Metric Type, AND fall into the exact same Time Bucket
          GROUP BY
            nm.resource_id,
            nm.metric_name,
            nm.metric_type,
            time_bucket(CAST(:bucketWidth AS INTERVAL), nm.period_start)
            
          -- Sort the resulting buckets chronologically
          ORDER BY periodStart ASC
          """,
      nativeQuery = true)
  List<AggregatedMetric> findAggregatedMetricsByPeriod(
      @Param("fromDate") OffsetDateTime fromDate,
      @Param("toDate") OffsetDateTime toDate,
      @Param("bucketWidth") String bucketWidth);
}