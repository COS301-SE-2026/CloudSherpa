package com.cloudsherpa.lib.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.StatusEnum;

public interface CloudAccountRepository extends JpaRepository<CloudAccount, UUID> {
  // Find all accounts for a specific connection
  List<CloudAccount> findByConnectionId(UUID connectionId);

  // Find all accounts of a specific type
  List<CloudAccount> findByAccountType(String accountType);

  // Find all accounts for a specific connection and type
  List<CloudAccount> findByConnectionIdAndAccountType(UUID connectionId, String accountType);

  // Find by CloudConnection object
  List<CloudAccount> findByConnection(CloudConnection connection);
  
  // Find all active accounts due for usage ingestion
   @Query("""
    SELECT a
    FROM CloudAccount a
    JOIN FETCH a.connection c
    WHERE c.status = :status
      AND a.nextUsageIngestion <= :now
    """)
    List<CloudAccount> findAccountsDueForUsageIngestion(
    @Param("now") OffsetDateTime now,
    @Param("status") StatusEnum status);  

  // Find all active accounts due for billing ingestion
  @Query("""
    SELECT a
    FROM CloudAccount a
    JOIN FETCH a.connection c
    WHERE c.status = :status
      AND a.nextBillingIngestion <= :now
    """)
    List<CloudAccount> findAccountsDueForBillingIngestion(
    @Param("now") OffsetDateTime now,
    @Param("status") StatusEnum status); 

    @Query("""
      SELECT MAX(a.lastBillingIngestion)
      FROM CloudAccount a
      JOIN a.connection c
      WHERE c.userId = :userId
      """)
    OffsetDateTime findLatestBillingIngestionByUserId(@Param("userId") UUID userId);
  }
