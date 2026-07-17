package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.NormalizedCosts;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NormalizedCostsRepository extends JpaRepository<NormalizedCosts, UUID> {
  List<NormalizedCosts> findByUsageStartTimeBetween(OffsetDateTime from, OffsetDateTime to);
  List<NormalizedCosts> findByResourceId(UUID resourceId);
}