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
@Table(name = "cloud_credential", schema = "public")
public class CloudCredential {

  @Id
  @Column(name = "credential_id", nullable = false)
  private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false, insertable = false, updatable = false)
  private CloudAccount account;

  @Column(name = "provider", nullable = false, length = 50)
  private String provider;

  @Column(name = "credential_type", nullable = false, length = 50)
  private String credentialType;

  @Column(name = "credential_value", nullable = false, columnDefinition = "text")
  private String credentialValue;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  protected CloudCredential() {}

  public CloudCredential(
      UUID id,
      UUID accountId,
      String provider,
      String credentialType,
      String credentialValue,
      OffsetDateTime createdAt) {
    this.id = id;
    this.accountId = accountId;
    this.provider = provider;
    this.credentialType = credentialType;
    this.credentialValue = credentialValue;
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

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getCredentialType() {
    return credentialType;
  }

  public void setCredentialType(String credentialType) {
    this.credentialType = credentialType;
  }

  public String getCredentialValue() {
    return credentialValue;
  }

  public void setCredentialValue(String credentialValue) {
    this.credentialValue = credentialValue;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
