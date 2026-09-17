package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.WidgetThreshold;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WidgetThresholdRepository extends JpaRepository<WidgetThreshold, UUID> {

  List<WidgetThreshold> findByWidgetIdAndMetricNameAndEnabledTrue(UUID widgetId, String metricName);

  List<WidgetThreshold> findByWidgetId(UUID widgetId);

  List<WidgetThreshold> findByUserId(UUID userId);
}