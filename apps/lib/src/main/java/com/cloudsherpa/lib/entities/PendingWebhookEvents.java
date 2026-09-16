package com.cloudsherpa.lib.entities;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pending_webhook_events", schema = "public")
public class PendingWebhookEvents {

    @Id 
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne
    @Column(name = "cloud_account", nullable = false) 
    private CloudAccount cloudAccountId;

    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;
}
