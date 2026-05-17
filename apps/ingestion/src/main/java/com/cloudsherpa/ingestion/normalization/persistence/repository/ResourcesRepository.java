package com.cloudsherpa.ingestion.normalization.persistence.repository;

import com.cloudsherpa.ingestion.normalization.persistence.entity.CloudAccount;
import com.cloudsherpa.ingestion.normalization.persistence.entity.Resources;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourcesRepository extends JpaRepository<Resources, UUID> {
  // Find all resources for a specific account
  List<Resources> findByAccountId(UUID accountId);

  // Find all resources of a specific type
  List<Resources> findByResourceType(String resourceType);

  // Find all resources for a specific account and type
  List<Resources> findByAccountIdAndResourceType(UUID accountId, String resourceType);

  // Find by CloudAccount object
  List<Resources> findByAccount(CloudAccount account);
}
