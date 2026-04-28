// Used https://jakarta.ee/learn/docs/jakartaee-tutorial/current/persist/persistence-intro/persistence-intro.html for assistance

package com.cloudsherpa.analytics.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.OffsetDateTime;
import jakarta.persistence.PrePersist;

import java.util.UUID;
import jakarta.persistence.Id;


@Entity
// Table name is the same as the environment_reference table in analytics-schema.sql
@Table(name = "environment_reference") 
public class EnvironmentReference 
{
    // The following variables are directly mapped to column names in the environment_reference table in analytics-schema.sql
    // Automatically generates a UUID for each record
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "environment_id", nullable = false, updatable = false)
    private UUID environmentId;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public EnvironmentReference() 
    {
        // required by JPA to have a no-argument default constructor
    }

    public EnvironmentReference(String provider) 
    {
        this.provider = provider;
    }

    // This method should execute automatically before the entity is inserted into the database for the first time
    // It will NOT trigger on SQL UPDATE statements
    @PrePersist
    private void generateCreatedAt() 
    {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getEnvironmentId() 
    {
        return environmentId;
    }

    public String getProvider() 
    {
        return provider;
    }

    public OffsetDateTime getCreatedAt() 
    {
        return createdAt;
    }
}
