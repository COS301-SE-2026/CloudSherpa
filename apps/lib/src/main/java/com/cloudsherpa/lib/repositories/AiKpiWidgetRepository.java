package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.AiKpiWidget;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiKpiWidgetRepository
    extends JpaRepository<AiKpiWidget, UUID> {
  AiKpiWidget findByWidgetId(UUID widgetId);
}
