package com.cloudsherpa.service.auth.dto;

import java.util.UUID;

public class AuthUserResponse {
  private UUID userId;
  private String email;
  private String username;
  private String token;

  public AuthUserResponse(UUID userId, String email, String username, String token) {
    this.userId = userId;
    this.email = email;
    this.username = username;
    this.token = token;
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

  public String getToken() {
    return token;
  }
}
