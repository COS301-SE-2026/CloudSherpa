package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.Alert;
import com.cloudsherpa.lib.entities.AlertStatusEnum;
import com.cloudsherpa.lib.entities.AlertTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

  List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatusEnum status);

  List<Alert> findByAlertTypeOrderByCreatedAtDesc(AlertTypeEnum alertType);

  Optional<Alert> findByCanonicalKeyAndStatus(String canonicalKey, AlertStatusEnum status);
}