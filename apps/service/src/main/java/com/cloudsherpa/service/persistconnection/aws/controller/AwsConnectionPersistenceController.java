package com.cloudsherpa.service.persistconnection.aws.controller;

import com.cloudsherpa.service.persistconnection.aws.dto.PersistAwsConnectionRequest;
import com.cloudsherpa.service.persistconnection.aws.dto.UpdateAccountNameRequest;
import com.cloudsherpa.service.persistconnection.aws.dto.UpdateResourceStatusRequest;
import com.cloudsherpa.service.persistconnection.aws.service.AwsConnectionPersistenceService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aws")
@Validated
public class AwsConnectionPersistenceController {

  private final AwsConnectionPersistenceService persistenceService;

  public AwsConnectionPersistenceController(AwsConnectionPersistenceService persistenceService) {

    this.persistenceService = persistenceService;
  }

  @PostMapping("/connections")
  public ResponseEntity<Void> persistConnection(
      @AuthenticationPrincipal Jwt jwt, @RequestBody PersistAwsConnectionRequest request) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    UUID userId;
    try {
      userId = UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }

    PersistAwsConnectionRequest requestWithUser = request.withUserId(userId);
    persistenceService.persistConnection(requestWithUser);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/connections/{accountId}")
  public ResponseEntity<Void> deleteAccount(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID accountId) {

    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId;
    try {
      userId = UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }

    boolean deleted = persistenceService.deleteAccount(userId, accountId);

    if (!deleted) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/connections/{accountId}/name")
  public ResponseEntity<Void> setAccountDisplayName(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID accountId,
      @RequestBody UpdateAccountNameRequest request) {

    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId;
    try {
      userId = UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }

    boolean updated = persistenceService.updateAccountName(userId, accountId, request.name());

    if (!updated) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/resources/{resourceId}/status")
  public ResponseEntity<Void> setResourceStatus(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID resourceId,
      @RequestBody UpdateResourceStatusRequest request) {

    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId;
    try {
      userId = UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }

    boolean updated = persistenceService.updateResourceStatus(userId, resourceId, request.status());

    if (!updated) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.noContent().build();
  }
}
