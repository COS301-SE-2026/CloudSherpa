package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.WidgetChart;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WidgetChartRepository extends JpaRepository<WidgetChart, UUID> {
  List<WidgetChart> findByWidgetId(UUID widgetId);
}