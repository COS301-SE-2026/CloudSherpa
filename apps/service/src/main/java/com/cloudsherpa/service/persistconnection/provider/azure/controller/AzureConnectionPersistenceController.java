package com.cloudsherpa.service.persistconnection.provider.azure.controller;

import com.cloudsherpa.service.persistconnection.provider.azure.dto.PersistAzureConnectionRequest;
import com.cloudsherpa.service.persistconnection.provider.azure.service.AzureConnectionPersistenceService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/azure")
@Validated
public class AzureConnectionPersistenceController {

  private final AzureConnectionPersistenceService persistenceService;

  public AzureConnectionPersistenceController(
      AzureConnectionPersistenceService persistenceService) {

    this.persistenceService = persistenceService;
  }

  @PostMapping("/connections")
  public ResponseEntity<Void> persistConnection(
      @AuthenticationPrincipal Jwt jwt, @RequestBody PersistAzureConnectionRequest request) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    UUID userId;
    try {
      userId = UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }

    PersistAzureConnectionRequest requestWithUser = request.withUserId(userId);
    persistenceService.persistConnection(requestWithUser);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
