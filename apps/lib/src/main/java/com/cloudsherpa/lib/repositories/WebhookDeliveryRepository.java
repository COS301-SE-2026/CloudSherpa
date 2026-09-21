package com.cloudsherpa.lib.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudsherpa.lib.entities.WebhookDelivery;
import com.cloudsherpa.lib.entities.WebhookDeliveryStatusEnum;

import jakarta.persistence.LockModeType;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM WebhookDelivery d WHERE d.webhookDeliveryId = :deliveryId AND d.deliveryStatus = :status")
    public Optional<WebhookDelivery> findDeliveryForUpdate(UUID deliveryId, WebhookDeliveryStatusEnum status);

    @Query("SELECT d FROM WebhookDelivery d WHERE d.deliveryStatus = :status AND d.nextAttemptAt <= CURRENT_TIMESTAMP ORDER BY d.nextAttemptAt")
    public List<WebhookDelivery> findDeliveriesForRetry(@Param("status") WebhookDeliveryStatusEnum status, Pageable pageable);

      @Query("""
      SELECT d
      FROM WebhookDelivery d
      LEFT JOIN d.webhook w
      LEFT JOIN d.cloudAccount a
      WHERE (
          :search IS NULL
          OR LOWER(w.webhookName) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(d.eventType) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(a.displayName) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(CAST(d.deliveryStatus AS string))
              LIKE LOWER(CONCAT('%', :search, '%'))
          OR CAST(d.responseCode AS string)
              LIKE CONCAT('%', :search, '%')
      )
      AND (:webhookId IS NULL OR w.webhookId = :webhookId)
      AND (:deliveryStatus IS NULL OR d.deliveryStatus = :deliveryStatus)
      """)
  Page<WebhookDelivery> findDeliveries(
      @Param("search") String search,
      @Param("webhookId") UUID webhookId,
      @Param("deliveryStatus") WebhookDeliveryStatusEnum status,
      Pageable pageable);

}
