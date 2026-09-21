package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_message", schema = "public")
public class AiMessage {

  @Id
  @Column(name = "message_id")
  private UUID messageId;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 20)
  private AiMessageRole role;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected AiMessage() {
  }

  private AiMessage(Builder builder) {
    this.messageId = builder.messageId;
    this.sessionId = builder.sessionId;
    this.role = builder.role;
    this.content = builder.content;
    this.createdAt = builder.createdAt;
  }

  public UUID getMessageId() {
    return messageId;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public AiMessageRole getRole() {
    return role;
  }

  public String getContent() {
    return content;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID messageId;
    private UUID sessionId;
    private AiMessageRole role;
    private String content;
    private OffsetDateTime createdAt;

    public Builder messageId(UUID messageId) {
      this.messageId = messageId;
      return this;
    }

    public Builder sessionId(UUID sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder role(AiMessageRole role) {
      this.role = role;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Builder createdAt(OffsetDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public AiMessage build() {
      return new AiMessage(this);
    }
  }

  public enum AiMessageRole {
    USER,
    ASSISTANT
  }
}
