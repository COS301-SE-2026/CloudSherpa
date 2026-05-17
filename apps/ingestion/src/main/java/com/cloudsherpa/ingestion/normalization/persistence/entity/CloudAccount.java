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
@Table(name = "cloud_accounts")
public class CloudAccount {

  @Id
  @Column(name = "account_id", nullable = false)
  private UUID id;

  @Column(name = "connection_id", nullable = false)
  private UUID connectionId;

  @ManyToOne
  @JoinColumn(name = "connection_id", nullable = false, insertable = false, updatable = false)
  private CloudConnections connection;

  @Column(name = "account_type", nullable = false, length = 255)
  private String accountType;

  @Column(name = "display_name", length = 255)
  private String displayName;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  protected CloudAccount() {}

  public CloudAccount(
      UUID id,
      UUID connectionId,
      String accountType,
      String displayName,
      OffsetDateTime createdAt) {
    this.id = id;
    this.connectionId = connectionId;
    this.accountType = accountType;
    this.displayName = displayName;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getConnectionId() {
    return connectionId;
  }

  public CloudConnections getConnection() {
    return connection;
  }

  public String getAccountType() {
    return accountType;
  }

  public void setAccountType(String accountType) {
    this.accountType = accountType;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
