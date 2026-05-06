// Used
// https://jakarta.ee/learn/docs/jakartaee-tutorial/current/persist/persistence-intro/persistence-intro.html for assistance
package com.cloudsherpa.ingestion.normalization.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
// Table name is the same as the environment_reference table in sherpadb-schema.sql
@Table(name = "environment_reference")
public class EnvironmentReference {
  // The following variables are directly mapped to column names in the environment_reference table
  // in sherpadb-schema.sql
  // Automatically generates a UUID for each record
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "environment_id", nullable = false, updatable = false)
  private UUID environmentId;

  @Column(name = "provider", nullable = false, length = 50)
  private String provider;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  public EnvironmentReference() {
    // required by JPA to have a no-argument default constructor
  }

  public EnvironmentReference(String provider, OffsetDateTime createdAt) {
    this.provider = provider;
    this.createdAt = createdAt;
  }

  public UUID getEnvironmentId() {
    return environmentId;
  }

  public String getProvider() {
    return provider;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
