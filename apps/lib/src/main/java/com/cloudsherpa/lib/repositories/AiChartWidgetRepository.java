package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.AiChartWidget;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChartWidgetRepository
    extends JpaRepository<AiChartWidget, UUID> {
  AiChartWidget findByWidgetId(UUID widgetId);
}
