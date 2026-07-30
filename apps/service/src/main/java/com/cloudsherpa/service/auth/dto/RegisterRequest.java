package com.cloudsherpa.service.auth.dto;

public class RegisterRequest {
  private String email;
  private String username;
  private String password;

  public RegisterRequest() {} // For jakson deserialization

  public RegisterRequest(String email, String username, String password) { // Testing utility
    this.email = email;
    this.username = username;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
