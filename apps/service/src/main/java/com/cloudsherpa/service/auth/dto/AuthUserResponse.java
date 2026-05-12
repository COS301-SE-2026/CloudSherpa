package com.cloudsherpa.service.auth.dto;

import java.util.UUID;

public class AuthUserResponse {
  private UUID userId;
  private String email;
  private String username;

  public AuthUserResponse(UUID userId, String email, String username) {
    this.userId = userId;
    this.email = email;
    this.username = username;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }
}
