package com.cloudsherpa.lib.entities;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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

  @Column(name = "webhook_id")
  private UUID webhookId;

  @Column(name = "event_id", nullable = false)
  private UUID eventId;

  @Column(name = "cloud_account")
  private UUID cloudAccountId;

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

  protected WebhookDelivery() {}

  public WebhookDelivery(
      UUID webhookDeliveryId,
      UUID webhookId,
      UUID eventId,
      UUID cloudAccountId,
      String eventType,
      Instant eventTimestamp,
      JsonNode payload,
      WebhookDeliveryStatusEnum deliveryStatus,
      Integer responseCode,
      Integer attemptCount) {
    this.webhookDeliveryId = webhookDeliveryId;
    this.webhookId = webhookId;
    this.eventId = eventId;
    this.cloudAccountId = cloudAccountId;
    this.eventType = eventType;
    this.eventTimestamp = eventTimestamp;
    this.payload = payload;
    this.deliveryStatus = deliveryStatus;
    this.responseCode = responseCode;
    this.attemptCount = attemptCount;
  }

  public UUID getWebhookDeliveryId() {
    return webhookDeliveryId;
  }

  public UUID getWebhookId() {
    return webhookId;
  }

  public UUID getEventId() {
    return eventId;
  }

  public UUID getCloudAccountId() {
    return cloudAccountId;
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

  public void setWebhookId(UUID webhookId) {
    this.webhookId = webhookId;
  }

  public void setEventId(UUID eventId) {
    this.eventId = eventId;
  }

  public void setCloudAccountId(UUID cloudAccountId) {
    this.cloudAccountId = cloudAccountId;
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
}
