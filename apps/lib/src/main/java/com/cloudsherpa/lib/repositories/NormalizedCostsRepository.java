package com.cloudsherpa.lib.repositories;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudsherpa.lib.entities.NormalizedCosts;

public interface NormalizedCostsRepository extends JpaRepository<NormalizedCosts, UUID> {
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
        AND nc.chargeId IN (:chargeIds)
      """)
  BigDecimal sumTotalCostBetweenForResources(
      @Param("fromDate") OffsetDateTime fromDate,
      @Param("toDate") OffsetDateTime toDate,
      @Param("chargeIds") List<String> chargeIds);
    
    @Query(
    value = """
        SELECT DISTINCT ON (nc.charge_id) nc.*
        FROM normalized_costs nc
        ORDER BY nc.charge_id
        """,
    nativeQuery = true)
    List<NormalizedCosts> findDistinctByChargeId();
}