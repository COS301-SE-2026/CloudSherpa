package com.cloudsherpa.ingestion.normalization.persistence.repository;

import com.cloudsherpa.ingestion.normalization.persistence.entity.CloudAccount;
import com.cloudsherpa.ingestion.normalization.persistence.entity.Resource;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
  // Find all resources for a specific account
  List<Resource> findByAccountId(UUID accountId);

  // Find all resources of a specific type
  List<Resource> findByResourceType(String resourceType);

  // Find all resources for a specific account and type
  List<Resource> findByAccountIdAndResourceType(UUID accountId, String resourceType);

  // Find by CloudAccount object
  List<Resource> findByAccount(CloudAccount account);
}
