package com.cloudsherpa.service.auth.controller;

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
  private final Duration tokenExpiryMinutes;
  private static final Duration DEFAULT_EXPIRY = Duration.ofMinutes(60);

  private final boolean authCookieSecure;

  public AuthController(
      AuthService authService,
      @Value("${auth.jwt.exp-minutes:60}") long tokenExpiryMinutes,
      @Value("${auth.cookie.secure:true}") boolean authCookieSecure) {
    this.authService = authService;
    this.tokenExpiryMinutes =
        tokenExpiryMinutes > 0 ? Duration.ofMinutes(tokenExpiryMinutes) : DEFAULT_EXPIRY;
    this.authCookieSecure = authCookieSecure;
  }

  @Operation(summary = "Register a new user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User successfully registered",
            content = @Content(schema = @Schema(implementation = AuthUserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
      })
  @PostMapping("/register")
  public ResponseEntity<AuthUserResponse> register(@RequestBody RegisterRequest request) {
    AuthUserResponse response = authService.register(request);

    ResponseCookie cookie =
        ResponseCookie.from("auth_token", response.getToken())
            .httpOnly(true)
            .secure(authCookieSecure) // ! true in production HTTPS
            .sameSite("Strict")
            .path("/")
            .maxAge(tokenExpiryMinutes.toSeconds())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(response);
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
    AuthUserResponse response = authService.login(request);

    ResponseCookie cookie =
        ResponseCookie.from("auth_token", response.getToken())
            .httpOnly(true)
            .secure(authCookieSecure) // ! true in production HTTPS
            .sameSite("Strict")
            .path("/")
            .maxAge(tokenExpiryMinutes.toSeconds())
            .build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    ResponseCookie cookie =
        ResponseCookie.from("auth_token")
            .httpOnly(true)
            .secure(authCookieSecure)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
  }

  // auth/me endpoint here
  @GetMapping("/me")
  public AuthUserResponse me(JwtAuthenticationToken authentication) {
    Jwt jwt = authentication.getToken();

    return new AuthUserResponse(
        UUID.fromString(jwt.getSubject()),
        jwt.getClaimAsString("email"),
        jwt.getClaimAsString("userId"),
        "");
  }
}
