package com.cloudsherpa.lib.entities;

import java.util.List;
import java.util.UUID;

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

    @Column(name = "event_types", nullable = false)
    private List<String> eventTypes;

    @Enumerated(EnumType.STRING)
    @Column(name = "webhook_status", nullable = false, columnDefinition = "public.webhook_status_enum")
    private WebhookStatusEnum webhookStatus;

    protected Webhook() {}

    public Webhook(
        UUID webhookId,
        String webhookName,
        List<String> eventTypes,
        WebhookStatusEnum webhookStatus) {
        this.webhookId = webhookId;
        this.webhookName = webhookName;
        this.eventTypes = eventTypes;
        this.webhookStatus = webhookStatus;
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
}
