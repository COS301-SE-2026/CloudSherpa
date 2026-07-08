package com.cloudsherpa.service.auth.service;

import com.cloudsherpa.lib.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
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
        .setSubject(user.getUsername())
        .claim("email", user.getEmail())
        .claim("tenantId", user.getId())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(expiresAt))
        .signWith(key)
        .compact();
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  private boolean isTokenExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  public UUID extractTenantId(String token) {
    final Claims claims = extractAllClaims(token);
    return UUID.fromString((String) claims.get("tenantId"));
  }
}
