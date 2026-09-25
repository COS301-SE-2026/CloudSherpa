package com.cloudsherpa.lib.entities;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.time.ZoneOffset;

@Entity
@Table(name = "refresh_tokens", schema = "public")
public class RefreshToken {

  @Id
  @GeneratedValue
  @Column(name = "token_id", nullable = false)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "revoked_at")
  private OffsetDateTime revokedAt;

  protected RefreshToken() {}

  public RefreshToken(
      User user,
      String tokenHash,
      OffsetDateTime expiresAt,
      OffsetDateTime createdAt) {
    this.user = user;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public OffsetDateTime getRevokedAt() {
    return revokedAt;
  }

  public void revoke() {
    this.revokedAt = OffsetDateTime.now(ZoneOffset.UTC);
  }

  public boolean isActive() {
    return revokedAt == null && expiresAt.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
  }
}