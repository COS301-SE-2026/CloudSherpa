package com.cloudsherpa.service.persistconnection.aws.controller;

import com.cloudsherpa.lib.entities.CloudAccount;
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
}
