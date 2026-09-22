package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.Threshold;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThresholdRepository extends JpaRepository<Threshold, UUID> {

  List<Threshold> findByResourceIdAndMetricNameAndEnabledTrue(UUID resourceId, String metricName);

  List<Threshold> findByResourceId(UUID resourceId);

  List<Threshold> findByUserId(UUID userId);

  List<Threshold> findByUserIdAndResourceId(UUID userId, UUID resourceId);
}