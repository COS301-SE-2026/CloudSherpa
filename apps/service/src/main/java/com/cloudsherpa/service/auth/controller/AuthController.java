package com.cloudsherpa.service.auth.controller;

import com.cloudsherpa.service.auth.dto.AuthUserResponse;
import com.cloudsherpa.service.auth.dto.LoginRequest;
import com.cloudsherpa.service.auth.dto.RegisterRequest;
import com.cloudsherpa.service.auth.service.AuthService;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;
  private final long tokenExpiryMinutes;

  public AuthController(
      AuthService authService, @Value("${auth.jwt.exp-minutes:60}") long tokenExpiryMinutes) {
    this.authService = authService;
    this.tokenExpiryMinutes = tokenExpiryMinutes;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthUserResponse> register(@RequestBody RegisterRequest request) {
    AuthUserResponse response = authService.register(request);

    ResponseCookie cookie =
        ResponseCookie.from("auth_token", response.getToken())
            .httpOnly(true)
            .secure(false) // ! true in production HTTPS
            .sameSite("Strict")
            .path("/")
            .maxAge(Duration.ofMinutes(tokenExpiryMinutes))
            .build();

    return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthUserResponse> login(@RequestBody LoginRequest request) {
    AuthUserResponse response = authService.login(request);

    ResponseCookie cookie =
        ResponseCookie.from("auth_token", response.getToken())
            .httpOnly(true)
            .secure(false) // ! true in production HTTPS
            .sameSite("Strict")
            .path("/")
            .maxAge(Duration.ofMinutes(tokenExpiryMinutes))
            .build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
  }
}
