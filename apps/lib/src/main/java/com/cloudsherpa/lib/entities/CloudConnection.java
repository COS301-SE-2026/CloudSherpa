package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cloud_connection", schema = "public")
public class CloudConnection {

  @Id
  @Column(name = "connection_id", nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "provider", nullable = false, columnDefinition = "public.provider_enum")
  private ProviderEnum provider;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false, columnDefinition = "public.status_enum")
  private StatusEnum status;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  protected CloudConnection() {}

  public CloudConnection(
      UUID id, UUID userId, ProviderEnum provider, StatusEnum status, OffsetDateTime createdAt) {
    this.id = id;
    this.userId = userId;
    this.provider = provider;
    this.status = status;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public User getUser() {
    return user;
  }

  public ProviderEnum getProvider() {
  return provider;
}

public StatusEnum getStatus() {
  return status;
}

public void setStatus(StatusEnum status) {
  this.status = status;
}
}
