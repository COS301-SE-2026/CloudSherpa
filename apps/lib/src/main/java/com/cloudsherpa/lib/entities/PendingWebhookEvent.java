package com.cloudsherpa.lib.entities;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pending_webhook_events", schema = "public")
public class PendingWebhookEvent {

    @Id 
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne
    @JoinColumn(name = "cloud_account", nullable = false) 
    private CloudAccount cloudAccount;

    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;

    protected PendingWebhookEvent() {}

    public PendingWebhookEvent(
        UUID eventId,
        UUID tenantId,
        CloudAccount cloudAccount,
        String eventType,
        Instant eventTimestamp,
        JsonNode payload) {
        this.eventId = eventId;
        this.tenantId = tenantId;
        this.cloudAccount = cloudAccount;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
        this.payload = payload;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public CloudAccount getCloudAccountId() {
        return cloudAccount;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public void setCloudAccountId(CloudAccount cloudAccount) {
        this.cloudAccount = cloudAccount;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setEventTimestamp(Instant eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }
}
