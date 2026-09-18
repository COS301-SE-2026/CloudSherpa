package com.cloudsherpa.lib.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cloudsherpa.lib.entities.Webhook;

public interface WebhookRepository extends JpaRepository<Webhook, UUID> {
    @Query(
        value = """
            SELECT *
            FROM webhook
            WHERE :eventType = ANY(event_types)
            """,
        nativeQuery = true
    )
    List<Webhook> findByEventType(String eventType);
}
