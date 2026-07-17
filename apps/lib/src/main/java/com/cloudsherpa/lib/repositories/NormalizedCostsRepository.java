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
      -- COALESCE ensures that if there are absolutely no costs for this time period 
      -- (which would normally return a SQL NULL), it returns a 0 instead. 
      SELECT COALESCE(SUM(nc.costAmount), 0) 
       
      FROM NormalizedCosts nc 
      
      -- Filters the records to only include those where the billing start time falls 
      -- within the provided from/to window. 
      -- AGGREGATION: Because 'usageStartTime' is the TimescaleDB hypertable 
      -- partition key, TimescaleDB will completely ignore irrelevant daily/weekly chunks 
      -- and only scan the exact partitions needed for this date range.
      WHERE nc.usageStartTime BETWEEN :fromDate AND :toDate
      """)
  BigDecimal sumTotalCostBetween(
      @Param("fromDate") OffsetDateTime fromDate, 
      @Param("toDate") OffsetDateTime toDate);

@Query(
      """
      SELECT COALESCE(SUM(nc.costAmount), 0) 
      
      FROM NormalizedCosts nc 
     
      WHERE nc.usageStartTime BETWEEN :fromDate AND :toDate
      
        -- Filters the sums to ONLY include rows where the internal
        -- UUID matches the list of UUIDs passed from the frontend.
        -- PERFORMANCE BOOST: Because we created the index 'ix_tenant_costs_resource_time'
        -- on (resource_id, usage_start_time), this lookup is lightning fast.
        AND nc.resourceId IN :resourceIds
      """)
  BigDecimal sumTotalCostBetweenForResources(
      @Param("fromDate") OffsetDateTime fromDate,
      @Param("toDate") OffsetDateTime toDate,
      @Param("resourceIds") List<UUID> resourceIds);
}