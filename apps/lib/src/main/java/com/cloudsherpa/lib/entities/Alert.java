package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "alerts")
public class Alert {

  @Id
  @Column(name = "alert_id", nullable = false, updatable = false)
  private UUID alertId;

  @Column(name = "user_id")
  private UUID userId;

  @ManyToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @Column(name = "widget_id")
  private UUID widgetId;

  @ManyToOne
  @JoinColumn(name = "widget_id", insertable = false, updatable = false)
  private Widget widget;

  @Column(name = "alert_type", nullable = false, length = 20)
  private String alertType;

  @Column(name = "severity", nullable = false, length = 20)
  private String severity;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "message")
  private String message;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload", columnDefinition = "jsonb")
  private Map<String, Object> payload;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "canonical_key")
  private String canonicalKey;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "last_seen")
  private OffsetDateTime lastSeen;

  @Column(name = "resolved_at")
  private OffsetDateTime resolvedAt;

  public Alert() {}

  public Alert(
      UUID userId,
      UUID widgetId,
      String alertType,
      String severity,
      String title,
      String message,
      Map<String, Object> payload,
      String status,
      String canonicalKey) {
    this.alertId = UUID.randomUUID();
    this.userId = userId;
    this.widgetId = widgetId;
    this.alertType = alertType;
    this.severity = severity;
    this.title = title;
    this.message = message;
    this.payload = payload;
    this.status = status;
    this.canonicalKey = canonicalKey;
  }

  public UUID getAlertId() {
    return alertId;
  }

  public void setAlertId(UUID alertId) {
    this.alertId = alertId;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public User getUser() {
    return user;
  }

  public UUID getWidgetId() {
    return widgetId;
  }

  public void setWidgetId(UUID widgetId) {
    this.widgetId = widgetId;
  }

  public Widget getWidget() {
    return widget;
  }

  public String getAlertType() {
    return alertType;
  }

  public void setAlertType(String alertType) {
    this.alertType = alertType;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public void setPayload(Map<String, Object> payload) {
    this.payload = payload;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCanonicalKey() {
    return canonicalKey;
  }

  public void setCanonicalKey(String canonicalKey) {
    this.canonicalKey = canonicalKey;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getLastSeen() {
    return lastSeen;
  }

  public void setLastSeen(OffsetDateTime lastSeen) {
    this.lastSeen = lastSeen;
  }

  public OffsetDateTime getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt(OffsetDateTime resolvedAt) {
    this.resolvedAt = resolvedAt;
  }
}