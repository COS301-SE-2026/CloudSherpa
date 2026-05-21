package com.cloudsherpa.ingestion.normalization.persistence.repository;

import com.cloudsherpa.ingestion.normalization.persistence.entity.CloudConnection;
import com.cloudsherpa.ingestion.normalization.persistence.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudConnectionRepository extends JpaRepository<CloudConnection, UUID> {
  // Find all connections for a specific user by userId
  List<CloudConnection> findByUserId(UUID userId);

  // Find all active connections for a specific user
  List<CloudConnection> findByUserIdAndStatus(UUID userId, String status);

  // Find all connections for a specific provider
  List<CloudConnection> findByProvider(String provider);

  // Find all connections for a specific user and provider
  List<CloudConnection> findByUserIdAndProvider(UUID userId, String provider);

  // Alternative: Find by User object directly
  List<CloudConnection> findByUser(User user);
}
