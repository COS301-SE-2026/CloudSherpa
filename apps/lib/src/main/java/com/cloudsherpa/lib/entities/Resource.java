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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

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

  @Column(name = "resource_identifier", nullable = false, length = 255)
  private String resourceIdentifier;

  @Column(name = "region", nullable = false, length = 100)
  private String region;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", columnDefinition = "public.status_enum")
  private StatusEnum status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "tags", columnDefinition = "jsonb")
  private Map<String, Object> tags;

  @Column(name = "last_updated")
  private OffsetDateTime lastUpdated;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  protected Resource() {
  }

  private Resource(Builder builder) {
    this.id = builder.id;
    this.accountId = builder.accountId;
    this.resourceType = builder.resourceType;
    this.resourceName = builder.resourceName;
    this.resourceIdentifier = builder.resourceIdentifier;
    this.region = builder.region;
    this.status = builder.status;
    this.tags = builder.tags;
    this.lastUpdated = builder.lastUpdated;
    this.createdAt = builder.createdAt;
  }

  public static class Builder {
    private UUID id;
    private UUID accountId;
    private String resourceType;
    private String resourceName;
    private String resourceIdentifier;
    private String region;
    private StatusEnum status;
    private Map<String, Object> tags;
    private OffsetDateTime lastUpdated;
    private OffsetDateTime createdAt;

    public Builder id(UUID id) {
      this.id = id;
      return this;
    }

    public Builder accountId(UUID accountId) {
      this.accountId = accountId;
      return this;
    }

    public Builder resourceType(String resourceType) {
      this.resourceType = resourceType;
      return this;
    }

    public Builder resourceName(String resourceName) {
      this.resourceName = resourceName;
      return this;
    }

    public Builder resourceIdentifier(String resourceIdentifier) {
      this.resourceIdentifier = resourceIdentifier;
      return this;
    }

    public Builder region(String region) {
      this.region = region;
      return this;
    }

    public Builder status(StatusEnum status) {
      this.status = status;
      return this;
    }

    public Builder tags(Map<String, Object> tags) {
      this.tags = tags;
      return this;
    }

    public Builder lastUpdated(OffsetDateTime lastUpdated) {
      this.lastUpdated = lastUpdated;
      return this;
    }

    public Builder createdAt(OffsetDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Resource build() {
      return new Resource(this);
    }
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

  public String getResourceName() {
    return resourceName;
  }

  public String getResourceIdentifier() {
    return resourceIdentifier;
  }

  public String getRegion() {
    return region;
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

  public StatusEnum getStatus() {
    return status;
  }
}
