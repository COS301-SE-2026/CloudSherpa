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

  @Column(name = "tags")
  private String tags;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  protected Resource() {}

  public Resource(
      UUID id, UUID accountId, String resourceType, String tags, OffsetDateTime createdAt) {
    this.id = id;
    this.accountId = accountId;
    this.resourceType = resourceType;
    this.tags = tags;
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

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
