package com.cloudsherpa.lib.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
}
