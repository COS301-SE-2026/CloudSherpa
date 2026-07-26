package com.cloudsherpa.service.sse;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/stream")
public class SseController {

  private final SseService sseService;

  SseController(SseService sseService) {
    this.sseService = sseService;
  }

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(JwtAuthenticationToken authentication) {
    Jwt jwt = authentication.getToken();
    UUID userId = UUID.fromString(jwt.getSubject());

    return sseService.subscribe(userId);
  }
}
