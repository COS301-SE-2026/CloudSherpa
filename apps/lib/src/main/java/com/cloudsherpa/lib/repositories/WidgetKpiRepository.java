package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.WidgetKpi;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WidgetKpiRepository extends JpaRepository<WidgetKpi, UUID> {
    List<WidgetKpi> findByWidgetId(UUID widgetId);
}