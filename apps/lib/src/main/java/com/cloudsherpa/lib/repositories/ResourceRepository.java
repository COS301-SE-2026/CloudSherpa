package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.Resource;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cloudsherpa.lib.projections.ResourceNames;
import org.springframework.data.jpa.repository.Query;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
  // Find all resources for a specific account
  List<Resource> findByAccountId(UUID accountId);

  // Find all resources of a specific type
  List<Resource> findByResourceType(String resourceType);

  // Find all resources for a specific account and type
  List<Resource> findByAccountIdAndResourceType(UUID accountId, String resourceType);

  // Find by CloudAccount object
  List<Resource> findByAccount(CloudAccount account);

  @Query("select r.id as id, r.resourceType as resourceType from Resource r")
  List<ResourceNames> findResourceNames();
}
