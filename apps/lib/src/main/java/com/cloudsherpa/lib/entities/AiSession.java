package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_session", schema = "public")
public class AiSession {

  @Id
  @Column(name = "session_id")
  private UUID sessionId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "last_activity", nullable = false)
  private OffsetDateTime lastActivity;

  @Column(name = "current_version_id")
  private UUID currentVersionId;

  protected AiSession() {
  }

  private AiSession(Builder builder) {
    this.sessionId = builder.sessionId;
    this.userId = builder.userId;
    this.createdAt = builder.createdAt;
    this.lastActivity = builder.lastActivity;
    this.currentVersionId = builder.currentVersionId;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public UUID getUserId() {
    return userId;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getLastActivity() {
    return lastActivity;
  }

  public UUID getCurrentVersionId() {
    return currentVersionId;
  }

  public void setCurrentVersionId(UUID currentVersionId) {
    this.currentVersionId = currentVersionId;
  }

  public void setLastActivity(OffsetDateTime lastActivity) {
    this.lastActivity = lastActivity;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID sessionId;
    private UUID userId;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastActivity;
    private UUID currentVersionId;

    public Builder sessionId(UUID sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder userId(UUID userId) {
      this.userId = userId;
      return this;
    }

    public Builder createdAt(OffsetDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder lastActivity(OffsetDateTime lastActivity) {
      this.lastActivity = lastActivity;
      return this;
    }

    public Builder currentVersionId(UUID currentVersionId) {
      this.currentVersionId = currentVersionId;
      return this;
    }

    public AiSession build() {
      return new AiSession(this);
    }
  }
}
