package com.cloudsherpa.service.auth.service;

import com.cloudsherpa.service.auth.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expirationMinutes;

  public JwtService(
      @Value("${auth.jwt.secret}") String secret,
      @Value("${auth.jwt.exp-minutes:60}") long expirationMinutes) {
    // Build an HMAC key from the configured secret.
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMinutes = expirationMinutes;
  }

  public String generateToken(User user) {
    // Capture the current time for issued-at and expiration calculations
    Instant now = Instant.now();

    // Compute the expiration timestamp
    Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

    // Build the JWT claims, then sign and serialize into a compact token string.
    return Jwts.builder()
        .setSubject(user.getId().toString())
        .claim("email", user.getEmail())
        .claim("username", user.getUsername())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(expiresAt))
        .signWith(key)
        .compact();
  }
}
