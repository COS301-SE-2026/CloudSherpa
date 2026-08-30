package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.projections.ResourceNames;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
  // Find all resources for a specific account
  List<Resource> findByAccountId(UUID accountId);

  // Find all resources of a specific type
  List<Resource> findByResourceType(String resourceType);

  // Find all resources for a specific account and type
  List<Resource> findByAccountIdAndResourceType(UUID accountId, String resourceType);

  // Find by CloudAccount object
  List<Resource> findByAccount(CloudAccount account);

  // Find a resource by its cloud identity
  Optional<Resource> findByAccountIdAndResourceTypeAndResourceIdentifierAndRegion(
      UUID accountId,
      String resourceType,
      String resourceIdentifier,
      String region);

  // Find a resource by its account, type, and identifier type
  List<Resource> findByAccountIdAndResourceTypeAndResourceIdentifierType(
      UUID accountId, String resourceType, String resourceIdentifierType);

  // Find the number of resources with a specific accountId
  long countByAccountId(UUID accountId);

  @Query("select r.id as id, r.resourceType as resourceName from Resource r")
  List<ResourceNames> findResourceNames();

  @Query("select r.account.connection.provider from Resource r where r.id = :resourceId")
  ProviderEnum findProviderByResourceId(@Param("resourceId") UUID resourceId);
}
