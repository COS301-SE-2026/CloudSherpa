package com.cloudsherpa.service.persistconnection.controller;

import com.cloudsherpa.service.persistconnection.dto.CloudAccountDetailsResponse;
import com.cloudsherpa.service.persistconnection.dto.CloudAccountPatchRequest;
import com.cloudsherpa.service.persistconnection.service.CloudAccountPersistenceService;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/aws", "/gcp", "/azure"})
@Validated
public class CloudAccountPersistenceController {

  private final CloudAccountPersistenceService accountPersistenceService;

  public CloudAccountPersistenceController(
      CloudAccountPersistenceService accountPersistenceService) {
    this.accountPersistenceService = accountPersistenceService;
  }

  @PatchMapping("/accounts/{accountId}")
  public ResponseEntity<CloudAccountDetailsResponse> updateAccount(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID accountId,
      @RequestBody CloudAccountPatchRequest request) {

    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId;

    try {
      userId = UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }

    try {
      CloudAccountDetailsResponse response =
          accountPersistenceService.updateAccount(userId, accountId, request);

      return ResponseEntity.ok(response);

    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
