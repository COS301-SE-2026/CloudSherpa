package com.cloudsherpa.lib.repositories;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudsherpa.lib.entities.NormalizedCosts;

import jakarta.transaction.Transactional;

public interface NormalizedCostsRepository extends JpaRepository<NormalizedCosts, String> {
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

    @Modifying
    @Transactional
    @Query(
        value = """
                INSERT INTO normalized_costs (
                    cost_id,
                    execution_id, 
                    resource_id, 
                    charge_id,
                    provider,
                    billing_account_id, 
                    service_name, 
                    charge_type,
                    cost_amount, 
                    currency, 
                    usage_start_time, 
                    usage_end_time, 
                    metadata
                )
                VALUES (
                    :#{#entity.costId}, :#{#entity.executionId}, :#{#entity.resourceId}, :#{#entity.chargeId} ,CAST(:#{#provider} AS public.provider_enum), :#{#entity.billingAccountId}, :#{#entity.serviceName}, CAST(:#{#chargeType} AS public.charge_type_enum), :#{#entity.costAmount}, CAST(:#{#currency} AS public.currency_enum), :#{#entity.usageStartTime}, :#{#entity.usageEndTime}, :#{#entity.metadata}
                ) ON CONFLICT (cost_id, usage_start_time) 
                DO UPDATE SET 
                    cost_amount = EXCLUDED.cost_amount,
                    execution_id = EXCLUDED.execution_id,
                    metadata = EXCLUDED.metadata;
                """,
                nativeQuery = true
    ) 
    int upsert(@Param("entity") NormalizedCosts entity, @Param("provider") String provider, @Param("chargeType") String chargeType, @Param("currency") String currency);
}