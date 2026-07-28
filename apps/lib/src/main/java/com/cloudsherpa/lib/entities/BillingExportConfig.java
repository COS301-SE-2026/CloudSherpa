package com.cloudsherpa.lib.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "billing_export_config", schema = "public")
public class BillingExportConfig {

  @Id
  @Column(name = "config_id", nullable = false)
  private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false, insertable = false, updatable = false)
  private CloudAccount account;

  @Column(name = "bucket_name", nullable = false, length = 255)
  private String bucketName;
  
  @Column(name = "bucket_region", nullable = false, length = 255)
  private String bucketRegion;

  @Column(name = "export_prefix", length = 255)
  private String exportPrefix;

  @Column(name = "export_name", nullable = false, length = 255)
  private String exportName;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  protected BillingExportConfig() {}

  public BillingExportConfig(
      UUID id,
      UUID accountId,
      String bucketName,
      String bucketRegion,
      String exportPrefix,
      String exportName,
      OffsetDateTime createdAt) {
    this.id = id;
    this.accountId = accountId;
    this.bucketName = bucketName;
    this.bucketRegion = bucketRegion;
    this.exportPrefix = exportPrefix;
    this.exportName = exportName;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public UUID getAccountId() { return accountId; }
  public CloudAccount getAccount() { return account; }
  public String getBucketName() { return bucketName; }
  public String getBucketRegion() { return bucketRegion; }
  public String getExportPrefix() { return exportPrefix; }
  public String getExportName() { return exportName; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
