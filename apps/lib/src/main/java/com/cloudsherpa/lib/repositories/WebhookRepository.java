package com.cloudsherpa.lib.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudsherpa.lib.entities.Webhook;

public interface WebhookRepository extends JpaRepository<Webhook, UUID> {
    // When the cloud_accounts array is empty the convention is that the webhook subscribes to all cloud accounts
    @Query(
        value = """
            SELECT *
            FROM webhooks
            WHERE webhook_status = 'ACTIVE'
            AND :eventType = ANY(event_types)
            AND (cardinality(cloud_accounts) = 0 OR (:cloudAccountId IS NULL OR :cloudAccountId = ANY(cloud_accounts)))
            """,
        nativeQuery = true
    )
    List<Webhook> findSubscribed(@Param("eventType") String eventType, @Param("cloudAccountId") UUID cloudAccountId);
}
