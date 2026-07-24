package com.cloudsherpa.service.persistconnection.aws.controller;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.service.persistconnection.aws.dto.CloudAccountDetailsResponse;
import com.cloudsherpa.service.persistconnection.aws.dto.ResourceCountResponse;
import com.cloudsherpa.service.persistconnection.aws.service.AwsConnectionQueryService;
import java.util.List;
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
public class AwsConnectionQueryController {

  private final AwsConnectionQueryService queryService;

  public AwsConnectionQueryController(AwsConnectionQueryService queryService) {

    this.queryService = queryService;
  }

  @GetMapping("/connections")
  public ResponseEntity<List<CloudAccount>> getAccountConnections(
      @AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId = UUID.fromString(jwt.getSubject());
    List<CloudAccount> accounts = queryService.getAccountConnections(userId);
    return ResponseEntity.status(HttpStatus.OK).body(accounts);
  }

  @GetMapping("/accounts/{accountId}")
  public ResponseEntity<CloudAccountDetailsResponse> getAccount(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID accountId) {

    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId = UUID.fromString(jwt.getSubject());

    List<CloudAccount> accounts = queryService.getAccountConnections(userId);

    if (accounts.stream().noneMatch(account -> account.getId().equals(accountId))) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.ok(queryService.getAccountDetails(accountId));
  }

  @GetMapping("/accounts/{accountId}/resources")
  public ResponseEntity<List<Resource>> getResourcesForAccount(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID accountId) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId = UUID.fromString(jwt.getSubject());
    List<CloudAccount> accounts = queryService.getAccountConnections(userId);
    if (accounts.stream().anyMatch(account -> account.getId().equals(accountId))) {
      List<Resource> resources = queryService.getResourcesForAccount(accountId);
      return ResponseEntity.status(HttpStatus.OK).body(resources);
    }
    // Not found if the user requests resources from an account they don't own
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  @GetMapping("/accounts/{accountId}/resources/count")
  public ResponseEntity<ResourceCountResponse> getResourceCount(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID accountId) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId = UUID.fromString(jwt.getSubject());
    List<CloudAccount> accounts = queryService.getAccountConnections(userId);
    if (accounts.stream().anyMatch(account -> account.getId().equals(accountId))) {
      long numResources = queryService.getResourceCountForAccount(accountId);
      return ResponseEntity.status(HttpStatus.OK).body(new ResourceCountResponse(numResources));
    }

    // Not found if the user requests resource count from an account they don't own
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }
}
