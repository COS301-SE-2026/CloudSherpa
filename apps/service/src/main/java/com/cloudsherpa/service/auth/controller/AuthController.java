package com.cloudsherpa.service.auth.controller;

import com.cloudsherpa.service.auth.dto.AuthSession;
import com.cloudsherpa.service.auth.dto.AuthUserResponse;
import com.cloudsherpa.service.auth.dto.LoginRequest;
import com.cloudsherpa.service.auth.dto.RegisterRequest;
import com.cloudsherpa.service.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {
  private final AuthService authService;
  private final Duration accessTokenExpiry;
  private final Duration refreshTokenExpiry;
  private final boolean authCookieSecure;

  public AuthController(
      AuthService authService,
      @Value("${auth.access-token.exp-minutes:15}") long accessTokenExpiryMinutes,
      @Value("${auth.refresh-token.exp-days:30}") long refreshTokenExpiryDays,
      @Value("${auth.cookie.secure:true}") boolean authCookieSecure) {

    this.authService = authService;
    this.accessTokenExpiry = Duration.ofMinutes(accessTokenExpiryMinutes);
    this.refreshTokenExpiry = Duration.ofDays(refreshTokenExpiryDays);
    this.authCookieSecure = authCookieSecure;
  }

  @Operation(summary = "Register a new user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User successfully registered",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
      })
  @PostMapping("/register")
  public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
    authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary = "Login user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated",
            content = @Content(schema = @Schema(implementation = AuthUserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Email or password is missing"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
      })
  @PostMapping("/login")
  public ResponseEntity<AuthUserResponse> login(@RequestBody LoginRequest request) {
    AuthSession session = authService.login(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, createAccessCookie(session.accessToken()).toString())
        .header(HttpHeaders.SET_COOKIE, createRefreshCookie(session.refreshToken()).toString())
        .body(session.user());
  }

  @Operation(summary = "Refresh authentication tokens")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tokens successfully refreshed"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
      })
  @PostMapping("/refresh")
  public ResponseEntity<AuthUserResponse> refresh(
      @CookieValue(name = "refresh_token", required = false) String refreshToken) {

    AuthSession session = authService.refresh(refreshToken);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, createAccessCookie(session.accessToken()).toString())
        .header(HttpHeaders.SET_COOKIE, createRefreshCookie(session.refreshToken()).toString())
        .body(session.user());
  }

  @Operation(summary = "Logout user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully logged out"),
      })
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refresh_token", required = false) String refreshToken) {

    authService.logout(refreshToken);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, clearAccessCookie().toString())
        .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
        .build();
  }

  @Operation(summary = "Get current authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Current authenticated user",
            content = @Content(schema = @Schema(implementation = AuthUserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated")
      })
  @GetMapping("/me")
  public AuthUserResponse me(JwtAuthenticationToken authentication) {
    Jwt jwt = authentication.getToken();

    return new AuthUserResponse(
        UUID.fromString(jwt.getSubject()),
        jwt.getClaimAsString("email"),
        jwt.getClaimAsString("username"));
  }

  private ResponseCookie createAccessCookie(String token) {
    return ResponseCookie.from("auth_token", token)
        .httpOnly(true)
        .secure(authCookieSecure)
        .sameSite("Strict")
        .path("/")
        .maxAge(accessTokenExpiry)
        .build();
  }

  private ResponseCookie createRefreshCookie(String token) {
    return ResponseCookie.from("refresh_token", token)
        .httpOnly(true)
        .secure(authCookieSecure)
        .sameSite("Strict")
        .path("/")
        .maxAge(refreshTokenExpiry)
        .build();
  }

  private ResponseCookie clearAccessCookie() {
    return ResponseCookie.from("auth_token")
        .httpOnly(true)
        .secure(authCookieSecure)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ZERO)
        .build();
  }

  private ResponseCookie clearRefreshCookie() {
    return ResponseCookie.from("refresh_token")
        .httpOnly(true)
        .secure(authCookieSecure)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ZERO)
        .build();
  }
}
