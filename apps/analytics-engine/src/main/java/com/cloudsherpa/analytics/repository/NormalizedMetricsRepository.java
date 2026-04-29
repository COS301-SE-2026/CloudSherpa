// Refer to EnvironmentReferenceRepository.java for most documentation

package com.cloudsherpa.analytics.repository;

import com.cloudsherpa.analytics.entity.NormalizedMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

public interface NormalizedMetricsRepository extends JpaRepository<NormalizedMetrics, UUID> 
{
    // Spring translates this method name into:
    // SELECT * FROM normalized_metrics WHERE recorded_at BETWEEN ? AND ?
    List<NormalizedMetrics> findByRecordedAtBetween(OffsetDateTime startTime, OffsetDateTime endTime);
}