package com.cloudsherpa.lib.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;

import jakarta.persistence.LockModeType;

public interface PendingWebhookEventRepository extends JpaRepository<PendingWebhookEvent, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM PendingWebhookEvent e WHERE e.eventId = :eventId")  
    public Optional<PendingWebhookEvent> findByIdForUpdate(UUID eventId);
}
