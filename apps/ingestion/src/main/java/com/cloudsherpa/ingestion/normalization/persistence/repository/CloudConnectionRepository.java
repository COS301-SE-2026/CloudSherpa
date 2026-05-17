package com.cloudsherpa.ingestion.normalization.persistence.repository;

import com.cloudsherpa.ingestion.normalization.persistence.entity.CloudConnections;
import com.cloudsherpa.ingestion.normalization.persistence.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudConnectionRepository extends JpaRepository<CloudConnections, UUID> {
  // Find all connections for a specific user by userId
  List<CloudConnections> findByUserId(UUID userId);

  // Find all active connections for a specific user
  List<CloudConnections> findByUserIdAndStatus(UUID userId, String status);

  // Find all connections for a specific provider
  List<CloudConnections> findByProvider(String provider);

  // Find all connections for a specific user and provider
  List<CloudConnections> findByUserIdAndProvider(UUID userId, String provider);

  // Alternative: Find by User object directly
  List<CloudConnections> findByUser(User user);
}
