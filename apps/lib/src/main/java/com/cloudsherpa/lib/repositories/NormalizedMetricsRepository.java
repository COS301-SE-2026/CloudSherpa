package com.cloudsherpa.lib.repositories;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudsherpa.lib.dtos.ResourceMetricEntry;
import com.cloudsherpa.lib.dtos.SegmentedMetric;
import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.entities.NormalizedMetrics;
import com.cloudsherpa.lib.projections.AggregatedMetric;
import com.cloudsherpa.lib.projections.OptimizationStatisticsAggregate;

public interface NormalizedMetricsRepository extends JpaRepository<NormalizedMetrics, UUID> {
  // Spring translates this method name into:
  // SELECT * FROM normalized_metrics WHERE recorded_at BETWEEN ? AND ?
  List<NormalizedMetrics> findByRecordedAtBetween(OffsetDateTime startTime, OffsetDateTime endTime);

  List<NormalizedMetrics> findByPeriodStartBetween(
      OffsetDateTime startTime, OffsetDateTime endTime);

  @Query(value = """
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
      """, nativeQuery = true)
  List<AggregatedMetric> findAggregatedMetricsByPeriod(
      @Param("fromDate") OffsetDateTime fromDate,
      @Param("toDate") OffsetDateTime toDate,
      @Param("bucketWidth") String bucketWidth);

  @Query(value = """
      SELECT
        nm.metric_value AS value,
        nm.period_start AS timestamp
        FROM normalized_metrics nm WHERE nm.resource_id = :resourceId AND metric_name = :metricName ORDER BY nm.period_start DESC;
      """, nativeQuery = true)
  List<TimestampedNumericDataPoint> getTimestampedMetricValues(@Param("resourceId") UUID metricId,
      @Param("metricName") String metricName, Pageable pageable);

  @Query(value = """
      SELECT DISTINCT
        nm.metric_value AS value,
        nm.period_start AS timestamp
        FROM normalized_metrics nm WHERE nm.resource_id = :resourceId AND nm.metric_name = :metricName AND nm.period_start > :from
        ORDER BY nm.period_start ASC;
      """, nativeQuery = true)
  List<TimestampedNumericDataPoint> getTimestampedMetricValuesAfterDate(@Param("resourceId") UUID resourceId,
      @Param("metricName") String metricName, @Param("from") Instant from);

    @Query(value = """
      SELECT
        AVG(nm.metric_value) AS value,
        time_bucket('10 minutes', period_start) AS timestamp
        FROM normalized_metrics nm WHERE nm.resource_id = :resourceId AND nm.metric_name = :metricName
        GROUP BY timestamp
        ORDER BY timestamp ASC;
      """, nativeQuery = true)
    List<TimestampedNumericDataPoint> getAggregatedTimestampedMetricValues(@Param("resourceId") UUID resourceId,
      @Param("metricName") String metricName, Pageable pageable);

    @Query(value = """
      SELECT
        AVG(nm.metric_value) AS value,
        time_bucket('10 minutes', period_start) AS timestamp
        FROM normalized_metrics nm WHERE nm.resource_id = :resourceId AND nm.metric_name = :metricName AND nm.period_start > :from
        GROUP BY timestamp
        ORDER BY timestamp ASC;
      """, nativeQuery = true)
    List<TimestampedNumericDataPoint> getAggregatedTimestampedMetricValuesAfterDate(@Param("resourceId") UUID resourceId,
      @Param("metricName") String metricName, @Param("from") Instant from);    

  @Query(value = """
      SELECT
          nm.resource_id AS resourceId,
          cc.provider AS provider,
          nm.metric_name AS metricName,
          MIN(nm.metric_value) AS minimumValue,
          MAX(nm.metric_value) AS maximumValue,
          AVG(nm.metric_value) AS averageValue,
          PERCENTILE_CONT(0.50)
              WITHIN GROUP (ORDER BY nm.metric_value) AS medianValue,
          PERCENTILE_CONT(0.95)
              WITHIN GROUP (ORDER BY nm.metric_value) AS p95Value,
          PERCENTILE_CONT(0.99)
              WITHIN GROUP (ORDER BY nm.metric_value) AS p99Value,
          STDDEV_POP(nm.metric_value) AS standardDeviation,
          COUNT(*) AS sampleCount
      FROM normalized_metrics nm
      JOIN resource r ON r.resource_id = nm.resource_id
      JOIN public.cloud_account ca ON ca.account_id = r.account_id
      JOIN public.cloud_connection cc ON cc.connection_id = ca.connection_id
      WHERE nm.period_start >= :windowStart
        AND nm.period_start < :windowEnd
      GROUP BY nm.resource_id, cc.provider, nm.metric_name
      """, nativeQuery = true)
  List<OptimizationStatisticsAggregate> aggregateStatistics(
      @Param("windowStart") OffsetDateTime windowStart,
      @Param("windowEnd") OffsetDateTime windowEnd);

  @Query(value = """
  SELECT nm.*
  FROM unnest((
    SELECT lttb(period_start, CAST(metric_value AS double precision), 300)
    FROM normalized_metrics WHERE resource_id = :resourceId AND metric_name = :metricName AND period_start >= :windowStart AND period_end <= :windowEnd
  )) AS sampled(time, value) INNER JOIN (SELECT * FROM normalized_metrics WHERE resource_id = :resourceId AND metric_name = :metricName AND period_start >= :windowStart AND period_end <= :windowEnd) nm ON sampled.time = nm.period_start;
      """, nativeQuery = true)
  List<NormalizedMetrics> getDownsampledNormalizedMetrics(
    @Param("resourceId") UUID resourceId,
    @Param("metricName") String metricName,
    @Param("windowStart") Instant windowStart,
    @Param("windowEnd") Instant windowEnd
  );

  @Query(
    value = """
        SELECT DISTINCT resource_id AS resourceId, metric_type AS metricType, metric_name AS metricName FROM normalized_metrics nm
        """,
        nativeQuery = true
  )
  List<ResourceMetricEntry> findDistinctResourceMetrics();

  @Query(
      value = """
          -- Observations hold the raw series.
          WITH observations AS (
              SELECT
                  period_start AS ts,
                  -- The lttb function expects values to be double precision.
                  CAST(metric_value AS double precision) AS value
              FROM normalized_metrics
              WHERE resource_id = :resourceId
                AND metric_name = :metricName
                AND period_start >= :windowStart
                AND period_end <= :windowEnd
          ),
          -- Add the timestamp of the prior metric.
          with_previous AS (
              SELECT
                  *,
                  -- LAG gets the previous row.
                  LAG(ts) OVER (ORDER BY ts) AS previous_ts
              FROM observations
          ),
          -- Detect gaps and assign segment IDs.
          segmented AS (
              SELECT
                  ts,
                  value,
                  SUM(
                      CASE
                          -- NULL for the first point in the series.
                          WHEN previous_ts IS NULL
                              OR ts - previous_ts > make_interval(
                                  secs => CAST(:gapSeconds AS double precision)
                              )
                              THEN 1
                          ELSE 0
                      END
                      -- The SUM is scoped from the first row through the current row.
                  ) OVER (ORDER BY ts ROWS UNBOUNDED PRECEDING) AS segment_id
              FROM with_previous
          ),
          -- Hold each segments number of points and duration.
          segment_sizes AS (
              SELECT
                  segment_id,
                  COUNT(*) AS point_count,
                  EXTRACT(EPOCH FROM MAX(ts) - MIN(ts)) AS duration_seconds
              FROM segmented
              GROUP BY segment_id
          ),
          -- Allocate a point budget to each segment.
          budgets AS (
              SELECT
                  segment_id,
                  point_count,
                  LEAST(
                      point_count,
                      GREATEST(
                          3,
                          -- Preserve small segments and avoid allocating more points than exist.
                          COALESCE(
                              CEIL(
                                  CAST(:pointBudget AS numeric)
                                  * duration_seconds
                                  / NULLIF(SUM(duration_seconds) OVER (), 0)
                              ),
                              3
                          )
                      )
                  )::integer AS target_points
              FROM segment_sizes
          ),
          sampled_segments AS (
              SELECT
                  s.segment_id,
                  lttb(s.ts, s.value, b.target_points) AS sampled
              FROM segmented s
              JOIN budgets b USING (segment_id)
              -- Only downsample segments that exceed their allocation.
              WHERE b.point_count > b.target_points
              GROUP BY s.segment_id, b.target_points
          ),
          result AS (
              -- Small segments fit their budget and are not in sampled_segments.
              SELECT
                  s.segment_id,
                  s.ts,
                  s.value
              FROM segmented s
              JOIN budgets b USING (segment_id)
              WHERE b.point_count <= b.target_points

              UNION ALL

              -- Expand each downsampled segment back into rows.
              SELECT
                  s.segment_id,
                  p.time AS ts,
                  p.value
              FROM sampled_segments s
              CROSS JOIN LATERAL unnest(s.sampled) AS p(time, value)
          ),
          joined_result AS (
            SELECT *
            FROM result r
            INNER JOIN normalized_metrics nm
                ON r.ts = nm.period_start
            WHERE nm.resource_id = :resourceId
                AND nm.metric_name = :metricName
                AND nm.period_start >= :windowStart
                AND nm.period_end <= :windowEnd
            )
          SELECT
              segment_id AS segmentId,
              metric_id AS metricId,
              resource_id AS resourceId,
              recorded_at AS recordedAt,
              metric_type AS metricType,
              metric_name AS metricName,
              metric_value AS metricValue,
              unit AS unit,
              currency AS currency,
              period_start AS periodStart,
              period_end AS periodEnd
          FROM joined_result
          ORDER BY period_start, segment_id
          """,
      nativeQuery = true)
  List<SegmentedMetric> getSegmentedDownsampledNormalizedMetrics(
      @Param("resourceId") UUID resourceId,
      @Param("metricName") String metricName,
      @Param("windowStart") Instant windowStart,
      @Param("windowEnd") Instant windowEnd,
      @Param("gapSeconds") double gapSeconds,
      @Param("pointBudget") int pointBudget);

  @Query(value = "SELECT set_config('jit', 'off', true)", nativeQuery = true)
  String disableJitForCurrentTransaction();

}
