package com.cloudsherpa.service.preferences.controller;

import com.cloudsherpa.service.preferences.dto.ThemeUpdateRequest;
import com.cloudsherpa.service.preferences.service.PreferencesService;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/preferences")
@Validated
public class PreferencesController {

  private final PreferencesService preferencesService;

  public PreferencesController(PreferencesService preferencesService) {
    this.preferencesService = preferencesService;
  }

  @GetMapping("/theme")
  public ResponseEntity<Map<String, String>> getTheme(@AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId = UUID.fromString(jwt.getSubject());
    String currentTheme = preferencesService.getUserTheme(userId);

    return ResponseEntity.ok(Collections.singletonMap("theme", currentTheme));
  }

  @PostMapping("/theme")
  public ResponseEntity<Void> updateTheme(
      @AuthenticationPrincipal Jwt jwt, @RequestBody ThemeUpdateRequest request) {

    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId = UUID.fromString(jwt.getSubject());
    preferencesService.updateTheme(userId, request.getTheme());

    return ResponseEntity.ok().build();
  }
}
