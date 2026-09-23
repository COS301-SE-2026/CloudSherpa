package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.AlertStatusEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

  List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatusEnum status);

  List<Alert> findByAlertTypeOrderByCreatedAtDesc(String alertType);

  List<Alert> findByWidgetId(UUID widgetId);

  Optional<Alert> findByCanonicalKeyAndStatus(String canonicalKey, AlertStatusEnum status);
}