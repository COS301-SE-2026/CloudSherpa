package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.ChartResource;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChartResourceRepository extends JpaRepository<ChartResource, UUID> {
    List<ChartResource> findByWidgetChartId(UUID chartId);
}