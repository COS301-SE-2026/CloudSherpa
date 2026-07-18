package com.cloudsherpa.lib.repositories;

import com.cloudsherpa.lib.entities.NormalizedCosts;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NormalizedCostsRepository extends JpaRepository<NormalizedCosts, UUID> {
  List<NormalizedCosts> findByUsageStartTimeBetween(OffsetDateTime from, OffsetDateTime to);
  List<NormalizedCosts> findByResourceId(UUID resourceId);

  @Query(
      """
      SELECT COALESCE(SUM(nc.costAmount), 0)
      FROM NormalizedCosts nc
      WHERE nc.usageStartTime BETWEEN :fromDate AND :toDate
      """)
  BigDecimal sumTotalCostBetween(
      @Param("fromDate") OffsetDateTime fromDate, @Param("toDate") OffsetDateTime toDate);

  @Query(
      """
      SELECT COALESCE(SUM(nc.costAmount), 0)
      FROM NormalizedCosts nc
      WHERE nc.usageStartTime BETWEEN :fromDate AND :toDate
        AND nc.resourceId IN (:resourceIds)
      """)
  BigDecimal sumTotalCostBetweenForResources(
      @Param("fromDate") OffsetDateTime fromDate,
      @Param("toDate") OffsetDateTime toDate,
      @Param("resourceIds") List<UUID> resourceIds);
}