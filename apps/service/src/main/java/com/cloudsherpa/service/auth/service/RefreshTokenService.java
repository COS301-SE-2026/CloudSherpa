package com.cloudsherpa.service.auth.service;

import com.cloudsherpa.lib.entities.RefreshToken;
import com.cloudsherpa.lib.entities.User;
import com.cloudsherpa.lib.repositories.RefreshTokenRepository;
import com.cloudsherpa.service.auth.dto.RotatedToken;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// Manages the lifecycle of refresh tokens.
@Service
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  // SecureRandom is used for cryptography; standard Random() is predictable and insecure.
  private final SecureRandom secureRandom;

  private final long refreshTokenDays;

  public RefreshTokenService(
      RefreshTokenRepository refreshTokenRepository,
      @Value("${auth.refresh-token.exp-days:30}") long refreshTokenDays) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenDays = refreshTokenDays;
    this.secureRandom = new SecureRandom();
  }

  // Entry point for creating a new token
  @Transactional
  public String create(User user) {
    return doCreate(user);
  }

  // Exchanges an existing valid refresh token for a brand new one.
  // This is called when an access token expires and the client needs a new session.
  @Transactional
  public RotatedToken rotate(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    String hashedToken = hash(rawToken);
    Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByTokenHash(hashedToken);

    if (tokenOptional.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    RefreshToken existing = tokenOptional.get();

    if (!existing.isActive()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    // Revoke the old token so it can never be used again (prevents replay attacks)
    existing.revoke();
    refreshTokenRepository.save(existing);

    // Generate a new token for the user to continue their session
    String replacementToken = doCreate(existing.getUser());

    return new RotatedToken(existing.getUser(), replacementToken);
  }

  // Invalidates a token (called during logout).
  @Transactional
  public void revoke(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      return;
    }

    String hashedToken = hash(rawToken);
    Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByTokenHash(hashedToken);

    if (tokenOptional.isPresent()) {
      RefreshToken token = tokenOptional.get();

      token.revoke();
      refreshTokenRepository.save(token);
    }
  }

  private String doCreate(User user) {
    String rawToken = generateRawToken();

    String tokenHash = hash(rawToken);

    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    OffsetDateTime expiresAt = now.plusDays(refreshTokenDays);

    RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt, now);
    refreshTokenRepository.save(refreshToken);

    return rawToken;
  }

  // Generates a secure, cryptographically random string.
  private String generateRawToken() {
    // Create an array of 32 bytes
    byte[] bytes = new byte[32];

    // Fill array with secure random data
    secureRandom.nextBytes(bytes);

    // Encode bytes into a Base64 string so it can be sent over HTTP.
    // "UrlEncoder().withoutPadding()" ensures there are no characters
    // that would break if the token is placed directly in a URL query parameter.
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    return encoder.encodeToString(bytes);
  }

  private String hash(String rawToken) {
    try {
      // Initialize the SHA-256 hashing algorithm
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // Convert the raw string into raw bytes
      byte[] inputBytes = rawToken.getBytes(StandardCharsets.UTF_8);

      // Perform the actual hashing
      byte[] hashBytes = digest.digest(inputBytes);

      // Convert the unreadable byte array back into a human-readable Hex string
      return HexFormat.of().formatHex(hashBytes);

    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
