package com.cloudsherpa.service.persistconnection.aws.controller;

import com.cloudsherpa.service.persistconnection.aws.dto.PersistAwsConnectionRequest;
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

    UUID userId = UUID.fromString(jwt.getSubject());
    PersistAwsConnectionRequest requestWithUser = request.withUserId(userId);
    persistenceService.persistConnection(requestWithUser);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
