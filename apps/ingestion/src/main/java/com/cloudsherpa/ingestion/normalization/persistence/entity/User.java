package com.cloudsherpa.ingestion.normalization.persistence.entity;

// This is the read-only entity for the ingestion service.
// Because the auth service already handles users by writing to SherpaDB,
// the ingestion service will need its own way to read from the db to get
// the fields that it needs.

// Note: There is some duplication here, but I thought another way to do it is
// to call the auth service endpoints to get user data but that would cause
// unnecessary latency

// The way I did it is that the User entity / repository for the ingestion service
// is specialized to only read from the Users table in its own manner.
// This means that the ingestion service only needs to know about the user_id so the
// entity that the ingestion service sets up is its own specialized version of the actual
// entity in the auth service.

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "user")
public class User {

  @Id
  @Column(name = "user_id", nullable = false)
  private UUID id;

  protected User() {}

  public User(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return id;
  }
}
