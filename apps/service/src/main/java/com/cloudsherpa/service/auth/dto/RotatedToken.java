package com.cloudsherpa.service.auth.dto;

import com.cloudsherpa.lib.entities.User;

public class RotatedToken {

  private final User user;
  private final String rawToken;

  public RotatedToken(User user, String rawToken) {
    this.user = user;
    this.rawToken = rawToken;
  }

  public User getUser() {
    return user;
  }

  public String getRawToken() {
    return rawToken;
  }
}
