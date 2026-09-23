package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "alert_type", nullable = false, columnDefinition = "public.alert_type_enum")
  private AlertTypeEnum alertType;

  @Column(name = "severity", nullable = false, length = 20)
  private String severity;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "message")
  private String message;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload", columnDefinition = "jsonb")
  private Map<String, Object> payload;

 @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false, columnDefinition = "public.alert_status_enum")
  private AlertStatusEnum status;

  @Column(name = "canonical_key")
  private String canonicalKey;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "last_seen")
  private OffsetDateTime lastSeen;

  @Column(name = "resolved_at")
  private OffsetDateTime resolvedAt;

  protected Alert() {}

  private Alert(Builder builder) {
    this.alertId = builder.alertId;
    this.userId = builder.userId;
    this.widgetId = builder.widgetId;
    this.alertType = builder.alertType;
    this.severity = builder.severity;
    this.title = builder.title;
    this.message = builder.message;
    this.payload = builder.payload;
    this.status = builder.status;
    this.canonicalKey = builder.canonicalKey;
    this.createdAt = builder.createdAt;
    this.lastSeen = builder.lastSeen;
    this.resolvedAt = builder.resolvedAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private UUID alertId = UUID.randomUUID();
    private UUID userId;
    private UUID widgetId;
    private AlertTypeEnum alertType;
    private String severity;
    private String title;
    private String message;
    private Map<String, Object> payload;
    private AlertStatusEnum status;
    private String canonicalKey;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastSeen;
    private OffsetDateTime resolvedAt;

    public Builder alertId(UUID alertId) {
      this.alertId = alertId;
      return this;
    }

    public Builder userId(UUID userId) {
      this.userId = userId;
      return this;
    }

    public Builder widgetId(UUID widgetId) {
      this.widgetId = widgetId;
      return this;
    }

    public Builder alertType(AlertTypeEnum alertType) {
      this.alertType = alertType;
      return this;
    }

    public Builder severity(String severity) {
      this.severity = severity;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder message(String message) {
      this.message = message;
      return this;
    }

    public Builder payload(Map<String, Object> payload) {
      this.payload = payload;
      return this;
    }

    public Builder status(AlertStatusEnum status) {
      this.status = status;
      return this;
    }

    public Builder canonicalKey(String canonicalKey) {
      this.canonicalKey = canonicalKey;
      return this;
    }

    public Builder createdAt(OffsetDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder lastSeen(OffsetDateTime lastSeen) {
      this.lastSeen = lastSeen;
      return this;
    }

    public Builder resolvedAt(OffsetDateTime resolvedAt) {
      this.resolvedAt = resolvedAt;
      return this;
    }

    public Alert build() {
      return new Alert(this);
    }
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

  public AlertTypeEnum getAlertType() {
    return alertType;
  }

  public void setAlertType(AlertTypeEnum alertType) {
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

  public AlertStatusEnum getStatus() {
    return status;
  }

  public void setStatus(AlertStatusEnum status) {
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