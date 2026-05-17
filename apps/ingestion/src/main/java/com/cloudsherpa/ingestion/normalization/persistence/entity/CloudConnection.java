package com.cloudsherpa.ingestion.normalization.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cloud_connections")
public class CloudConnection {

  @Id
  @Column(name = "connection_id", nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
  private User user;

  @Column(name = "provider", nullable = false, length = 50)
  private String provider;

  @Column(name = "status", nullable = false, length = 50)
  private String status;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  protected CloudConnection() {}

  public CloudConnection(
      UUID id, UUID userId, String provider, String status, OffsetDateTime createdAt) {
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

  public String getProvider() {
    return provider;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
