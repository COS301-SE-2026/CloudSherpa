package com.cloudsherpa.service.persistconnection.provider.gcp.controller;

import com.cloudsherpa.service.persistconnection.provider.gcp.dto.PersistGcpConnectionRequest;
import com.cloudsherpa.service.persistconnection.provider.gcp.service.GcpConnectionPersistenceService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gcp")
@Validated
public class GcpConnectionPersistenceController {

  private final GcpConnectionPersistenceService persistenceService;

  public GcpConnectionPersistenceController(GcpConnectionPersistenceService persistenceService) {

    this.persistenceService = persistenceService;
  }

  @PostMapping("/connections")
  public ResponseEntity<Void> persistConnection(
      @AuthenticationPrincipal Jwt jwt, @RequestBody PersistGcpConnectionRequest request) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    UUID userId;
    try {
      userId = UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }

    PersistGcpConnectionRequest requestWithUser = request.withUserId(userId);
    persistenceService.persistConnection(requestWithUser);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
