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
@Table(name = "cloud_account", schema = "public")
public class CloudAccount {

  @Id
  @Column(name = "account_id", nullable = false)
  private UUID id;

  @Column(name = "connection_id", nullable = false)
  private UUID connectionId;

  @ManyToOne
  @JoinColumn(name = "connection_id", nullable = false, insertable = false, updatable = false)
  private CloudConnection connection;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "account_type", nullable = false, columnDefinition = "public.account_type_enum")
  private AccountTypeEnum accountType;

  @Column(name = "display_name", length = 255)
  private String displayName;

  @Column(name = "ingestion_period", length = 50)
  private String ingestionPeriod;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "last_usage_ingestion")
  private OffsetDateTime lastUsageIngestion;

  @Column(name = "next_usage_ingestion")
  private OffsetDateTime nextUsageIngestion;

  @Column(name = "last_billing_ingestion")
  private OffsetDateTime lastBillingIngestion;

  @Column(name = "next_billing_ingestion")
  private OffsetDateTime nextBillingIngestion;

  @Column(name = "periodic_resource_discovery", nullable = false)
  private boolean periodicResourceDiscovery;

  @Column(name = "auto_adjust_ingestion_period", nullable = false)
  private boolean autoAdjustIngestionPeriod;

  @Column(name = "new_resources_active", nullable = false)
  private boolean newResourcesActive;

  @Column(name = "next_resource_scan")
  private OffsetDateTime nextResourceScan;

  protected CloudAccount() {
  }

  public CloudAccount(
      UUID id,
      UUID connectionId,
      AccountTypeEnum accountType,
      String displayName,
      String ingestionPeriod,
      boolean periodicResourceDiscovery,
      boolean autoAdjustIngestionPeriod,
      boolean newResourcesActive,
      OffsetDateTime nextResourceScan,
      OffsetDateTime createdAt,
      OffsetDateTime lastUsageIngestion,
      OffsetDateTime nextUsageIngestion,
      OffsetDateTime lastBillingIngestion,
      OffsetDateTime nextBillingIngestion) {
    this.id = id;
    this.connectionId = connectionId;
    this.accountType = accountType;
    this.displayName = displayName;
    this.ingestionPeriod = ingestionPeriod;
    this.periodicResourceDiscovery = periodicResourceDiscovery;
    this.autoAdjustIngestionPeriod = autoAdjustIngestionPeriod;
    this.newResourcesActive = newResourcesActive;
    this.nextResourceScan = nextResourceScan;
    this.createdAt = createdAt;
    this.lastUsageIngestion = lastUsageIngestion;
    this.nextUsageIngestion = nextUsageIngestion;
    this.lastBillingIngestion = lastBillingIngestion;
    this.nextBillingIngestion = nextBillingIngestion;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private UUID id;
    private UUID connectionId;
    private AccountTypeEnum accountType;
    private String displayName;
    private String ingestionPeriod;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastUsageIngestion;
    private OffsetDateTime nextUsageIngestion;
    private OffsetDateTime lastBillingIngestion;
    private OffsetDateTime nextBillingIngestion;

    private boolean periodicResourceDiscovery = false;
    private boolean autoAdjustIngestionPeriod = false;
    private boolean newResourcesActive = false;
    private OffsetDateTime nextResourceScan;

    public Builder id(UUID id) {
      this.id = id;
      return this;
    }

    public Builder connectionId(UUID connectionId) {
      this.connectionId = connectionId;
      return this;
    }

    public Builder accountType(AccountTypeEnum accountType) {
      this.accountType = accountType;
      return this;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder ingestionPeriod(String ingestionPeriod) {
      this.ingestionPeriod = ingestionPeriod;
      return this;
    }

    public Builder createdAt(OffsetDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder lastUsageIngestion(OffsetDateTime lastUsageIngestion) {
      this.lastUsageIngestion = lastUsageIngestion;
      return this;
    }

    public Builder nextUsageIngestion(OffsetDateTime nextUsageIngestion) {
      this.nextUsageIngestion = nextUsageIngestion;
      return this;
    }

    public Builder lastBillingIngestion(OffsetDateTime lastBillingIngestion) {
      this.lastBillingIngestion = lastBillingIngestion;
      return this;
    }

    public Builder nextBillingIngestion(OffsetDateTime nextBillingIngestion) {
      this.nextBillingIngestion = nextBillingIngestion;
      return this;
    }

    public Builder periodicResourceDiscovery(boolean periodicResourceDiscovery) {
      this.periodicResourceDiscovery = periodicResourceDiscovery;
      return this;
    }

    public Builder autoAdjustIngestionPeriod(boolean autoAdjustIngestionPeriod) {
      this.autoAdjustIngestionPeriod = autoAdjustIngestionPeriod;
      return this;
    }

    public Builder newResourcesActive(boolean newResourcesActive) {
      this.newResourcesActive = newResourcesActive;
      return this;
    }

    public Builder nextResourceScan(OffsetDateTime nextResourceScan) {
      this.nextResourceScan = nextResourceScan;
      return this;
    }

    public CloudAccount build() {
      return new CloudAccount(
          id,
          connectionId,
          accountType,
          displayName,
          ingestionPeriod,
          periodicResourceDiscovery,
          autoAdjustIngestionPeriod,
          newResourcesActive,
          nextResourceScan,
          createdAt,
          lastUsageIngestion,
          nextUsageIngestion,
          lastBillingIngestion,
          nextBillingIngestion);
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getConnectionId() {
    return connectionId;
  }

  public CloudConnection getConnection() {
    return connection;
  }

  public AccountTypeEnum getAccountType() {
    return accountType;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String name) {
    this.displayName = name;
  }

  public String getIngestionPeriod() {
    return ingestionPeriod;
  }

  public void setIngestionPeriod(String ingestionPeriod) {
    this.ingestionPeriod = ingestionPeriod;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getLastUsageIngestion() {
    return lastUsageIngestion;
  }

  public void setLastUsageIngestion(OffsetDateTime lastIngestion) {
    this.lastUsageIngestion = lastIngestion;
  }

  public OffsetDateTime getNextUsageIngestion() {
    return nextUsageIngestion;
  }

  public void setNextUsageIngestion(OffsetDateTime nextIngestion) {
    this.nextUsageIngestion = nextIngestion;
  }

  public OffsetDateTime getLastBillingIngestion() {
    return lastBillingIngestion;
  }

  public void setLastBillingIngestion(OffsetDateTime lastIngestion) {
    this.lastBillingIngestion = lastIngestion;
  }

  public OffsetDateTime getNextBillingIngestion() {
    return nextBillingIngestion;
  }

  public void setNextBillingIngestion(OffsetDateTime nextIngestion) {
    this.nextBillingIngestion = nextIngestion;
  }

  public boolean isPeriodicResourceDiscovery() {
    return periodicResourceDiscovery;
  }

  public void setPeriodicResourceDiscovery(boolean periodicResourceDiscovery) {
    this.periodicResourceDiscovery = periodicResourceDiscovery;
  }

  public boolean isAutoAdjustIngestionPeriod() {
    return autoAdjustIngestionPeriod;
  }

  public void setAutoAdjustIngestionPeriod(boolean autoAdjustIngestionPeriod) {
    this.autoAdjustIngestionPeriod = autoAdjustIngestionPeriod;
  }

  public boolean isNewResourcesActive() {
    return newResourcesActive;
  }

  public void setNewResourcesActive(boolean newResourcesActive) {
    this.newResourcesActive = newResourcesActive;
  }

  public OffsetDateTime getNextResourceScan() {
    return nextResourceScan;
  }

  public void setNextResourceScan(OffsetDateTime nextResourceScan) {
    this.nextResourceScan = nextResourceScan;
  }
}
