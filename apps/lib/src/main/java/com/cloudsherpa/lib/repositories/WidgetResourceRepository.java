package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.WidgetResource;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WidgetResourceRepository extends JpaRepository<WidgetResource, UUID> {
    List<WidgetResource> findByWidgetId(UUID widgetId);
}