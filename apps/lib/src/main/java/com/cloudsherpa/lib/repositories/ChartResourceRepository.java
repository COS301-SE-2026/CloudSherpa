package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.ChartResource;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChartResourceRepository extends JpaRepository<ChartResource, UUID> {
    // Resolves which widgets are tracking this resource/metric so we can look up their thresholds
    List<ChartResource> findByResourceIdAndMetricName(UUID resourceId, String metricName);

    List<ChartResource> findByWidgetChartId(UUID chartId);
}