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
@Table(name = "resource")
public class Resource {
  @Id
  @Column(name = "resource_id", nullable = false)
  private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false, insertable = false, updatable = false)
  private CloudAccount account;

  @Column(name = "resource_type", length = 255)
  private String resourceType;

  @Column(name = "resource_name", length = 255, nullable = false)
  private String resourceName;

  @Column(name = "status", length = 50)
  private String status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "tags", columnDefinition = "jsonb")
  private Map<String, Object> tags;

  @Column(name = "last_updated")
  private OffsetDateTime lastUpdated;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  protected Resource() {}

  public Resource(
      UUID id,
      UUID accountId,
      String resourceType,
      String resourceName,
      String status,
      Map<String, Object> tags,
      OffsetDateTime lastUpdated,
      OffsetDateTime createdAt) {
    this.id = id;
    this.accountId = accountId;
    this.resourceType = resourceType;
    this.resourceName = resourceName;
    this.status = status;
    this.tags = tags;
    this.lastUpdated = lastUpdated;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public CloudAccount getAccount() {
    return account;
  }

  public String getResourceType() {
    return resourceType;
  }

  public Map<String, Object> getTags() {
    return tags;
  }

  public OffsetDateTime getLastUpdated() {
    return lastUpdated;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}