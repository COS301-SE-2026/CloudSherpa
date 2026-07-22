package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.Widget;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardWidgetRepository extends JpaRepository<Widget, UUID> {
    List<Widget> findByDashboardId(UUID dashboardId);
}