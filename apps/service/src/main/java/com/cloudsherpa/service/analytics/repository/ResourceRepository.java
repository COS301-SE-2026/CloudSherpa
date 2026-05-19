package com.cloudsherpa.service.analytics.repository;

import com.cloudsherpa.service.analytics.entities.Resource;
import com.cloudsherpa.service.analytics.projections.ResourceNames;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {

  @Query("select r.id as id, r.resourceType as resourceType from Resource r")
  List<ResourceNames> findResourceNames();
}
