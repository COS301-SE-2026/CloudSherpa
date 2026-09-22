package com.cloudsherpa.lib.entities;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "webhooks")
public class Webhook {
    @Id
    @Column(name = "webhook_id", nullable = false)
    private UUID webhookId;

    @Column(name = "webhook_name", nullable = false)
    private String webhookName;

    @Column(name = "endpoint_url", nullable = false)
    private String endpointUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "event_types", nullable = false)
    private List<String> eventTypes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "cloud_accounts", nullable = false)
    private List<UUID> cloudAccounts;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "webhook_status", nullable = false, columnDefinition = "public.webhook_status_enum")
    private WebhookStatusEnum webhookStatus;

    @Column(name = "webhook_signing_key", nullable = false)
    String webhookSigningKey;

    protected Webhook() {}

    public Webhook(
        UUID webhookId,
        String webhookName,
        String endpointUrl,
        List<String> eventTypes,
        List<UUID> cloudAccounts,
        WebhookStatusEnum webhookStatus,
        String webhookSigningKey) {
        this.webhookId = webhookId;
        this.webhookName = webhookName;
        this.endpointUrl = endpointUrl;
        this.eventTypes = eventTypes;
        this.webhookStatus = webhookStatus;
        this.cloudAccounts = cloudAccounts;
        this.webhookSigningKey = webhookSigningKey;
    }

    public UUID getWebhookId() {
        return webhookId;
    }

    public String getWebhookName() {
        return webhookName;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public WebhookStatusEnum getWebhookStatus() {
        return webhookStatus;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public List<UUID> getCloudAccounts() {
        return cloudAccounts;
    }

    public void setWebhookName(String webhookName) {
        this.webhookName = webhookName;
    }

    public void setEventTypes(List<String> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public void setWebhookStatus(WebhookStatusEnum webhookStatus) {
        this.webhookStatus = webhookStatus;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public void setCloudAccounts(List<UUID> cloudAccounts) {
        this.cloudAccounts = cloudAccounts;
    }

    public String getSigningKey() {
        return webhookSigningKey;
    }
}
