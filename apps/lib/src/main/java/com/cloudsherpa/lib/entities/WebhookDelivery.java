package com.cloudsherpa.lib.entities;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "webhook_deliveries")
public class WebhookDelivery {

  @Id
  @Column(name = "webhook_delivery_id", nullable = false)
  private UUID webhookDeliveryId;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "webhook_id")
  private Webhook webhook;

  @Column(name = "event_id", nullable = false)
  private UUID eventId;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "cloud_account")
  private CloudAccount cloudAccount;

  @Column(name = "cloud_account_name", nullable = true)
  private String cloudAccountName;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "event_timestamp", nullable = false)
  private Instant eventTimestamp;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
  private JsonNode payload;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(
      name = "delivery_status",
      nullable = false,
      columnDefinition = "public.webhook_delivery_status_enum")
  private WebhookDeliveryStatusEnum deliveryStatus;

  @Column(name = "response_code")
  private Integer responseCode;

  @Column(name = "attempt_count", nullable = false)
  private Integer attemptCount;
  
  @Column(name = "next_attempt_at", nullable = true)
  private Instant nextAttemptAt;

  protected WebhookDelivery() {}

  private WebhookDelivery(Builder builder) {
    this.webhookDeliveryId = builder.webhookDeliveryId;
    this.webhook = builder.webhook;
    this.eventId = builder.eventId;
    this.cloudAccount = builder.cloudAccount;
    this.cloudAccountName = builder.cloudAccountName;
    this.eventType = builder.eventType;
    this.eventTimestamp = builder.eventTimestamp;
    this.payload = builder.payload;
    this.deliveryStatus = builder.deliveryStatus;
    this.responseCode = builder.responseCode;
    this.attemptCount = builder.attemptCount;
    this.nextAttemptAt = builder.nextAttemptAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private UUID webhookDeliveryId;
    private Webhook webhook;
    private UUID eventId;
    private CloudAccount cloudAccount;
    private String cloudAccountName;
    private String eventType;
    private Instant eventTimestamp;
    private JsonNode payload;
    private WebhookDeliveryStatusEnum deliveryStatus;
    private Integer responseCode;
    private Integer attemptCount;
    private Instant nextAttemptAt;

    public Builder webhookDeliveryId(UUID webhookDeliveryId) {
      this.webhookDeliveryId = webhookDeliveryId;
      return this;
    }

    public Builder webhook(Webhook webhook) {
      this.webhook = webhook;
      return this;
    }

    public Builder eventId(UUID eventId) {
      this.eventId = eventId;
      return this;
    }

    public Builder cloudAccount(CloudAccount cloudAccount) {
      this.cloudAccount = cloudAccount;
      return this;
    }

    public Builder cloudAccountName(String cloudAccountName) {
      this.cloudAccountName = cloudAccountName;
      return this;
    }

    public Builder eventType(String eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder eventTimestamp(Instant eventTimestamp) {
      this.eventTimestamp = eventTimestamp;
      return this;
    }

    public Builder payload(JsonNode payload) {
      this.payload = payload;
      return this;
    }

    public Builder deliveryStatus(WebhookDeliveryStatusEnum deliveryStatus) {
      this.deliveryStatus = deliveryStatus;
      return this;
    }

    public Builder responseCode(Integer responseCode) {
      this.responseCode = responseCode;
      return this;
    }

    public Builder attemptCount(Integer attemptCount) {
      this.attemptCount = attemptCount;
      return this;
    }

    public Builder nextAttemptAt(Instant nextAttemptAt) {
      this.nextAttemptAt = nextAttemptAt;
      return this;
    }

    public WebhookDelivery build() {
      return new WebhookDelivery(this);
    }
  }

  public UUID getWebhookDeliveryId() {
    return webhookDeliveryId;
  }

  public Webhook getWebhook() {
    return webhook;
  }

  public UUID getEventId() {
    return eventId;
  }

  public CloudAccount getCloudAccount() {
    return cloudAccount;
  }

  public String getCoudAccountName() {
    return cloudAccountName;
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

  public WebhookDeliveryStatusEnum getDeliveryStatus() {
    return deliveryStatus;
  }

  public Integer getResponseCode() {
    return responseCode;
  }

  public Integer getAttemptCount() {
    return attemptCount;
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public void setWebhook(Webhook webhook) {
    this.webhook = webhook;
  }

  public void setEventId(UUID eventId) {
    this.eventId = eventId;
  }

  public void setCloudAccount(CloudAccount cloudAccount) {
    this.cloudAccount = cloudAccount;
  }

  public void setCloudAccountName(String cloudAccountName) {
    this.cloudAccountName = cloudAccountName;
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

  public void setDeliveryStatus(WebhookDeliveryStatusEnum deliveryStatus) {
    this.deliveryStatus = deliveryStatus;
  }

  public void setResponseCode(Integer responseCode) {
    this.responseCode = responseCode;
  }

  public void setAttemptCount(Integer attemptCount) {
    this.attemptCount = attemptCount;
  }

  public void setNextAttemptAt(Instant nextAttemptAt) {
    this.nextAttemptAt = nextAttemptAt;
  }
}
