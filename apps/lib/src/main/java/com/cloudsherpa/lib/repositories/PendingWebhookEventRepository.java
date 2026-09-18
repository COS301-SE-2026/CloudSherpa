package com.cloudsherpa.lib.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudsherpa.lib.entities.PendingWebhookEvent;

public interface PendingWebhookEventRepository extends JpaRepository<PendingWebhookEvent, UUID> {
    
}
