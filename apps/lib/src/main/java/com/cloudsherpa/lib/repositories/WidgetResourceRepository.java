package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.WidgetResource;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WidgetResourceRepository extends JpaRepository<WidgetResource, UUID> {
}