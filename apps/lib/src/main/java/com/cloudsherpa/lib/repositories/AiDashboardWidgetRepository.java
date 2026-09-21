package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.AiDashboardWidget;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDashboardWidgetRepository
    extends JpaRepository<AiDashboardWidget, UUID> {

  List<AiDashboardWidget> findByDashboardVersionId(UUID dashboardVersionId);
}
