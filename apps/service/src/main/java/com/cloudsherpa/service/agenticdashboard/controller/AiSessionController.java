package com.cloudsherpa.service.agenticdashboard.controller;

import com.cloudsherpa.lib.entities.AiSession;
import com.cloudsherpa.service.agenticdashboard.dto.AiSessionResponseDto;
import com.cloudsherpa.service.agenticdashboard.service.AiSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/sessions")
@Tag(name = "AI Dashboard", description = "Agentic Dashboard Construction Operations")
public class AiSessionController {

  private final AiSessionService aiSessionService;

  public AiSessionController(AiSessionService aiSessionService) {
    this.aiSessionService = aiSessionService;
  }

  @Operation(
      summary = "Create an AI dashboard session",
      description =
          "Creates a new isolated AI dashboard construction session for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "AI dashboard session created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AiSessionResponseDto.class))),
        @ApiResponse(
            responseCode = "401",
            description = "User is not authenticated",
            content = @Content)
      })
  @PostMapping
  public ResponseEntity<AiSessionResponseDto> createSession(@AuthenticationPrincipal Jwt jwt) {

    UUID userId = UUID.fromString(jwt.getSubject());

    AiSession session = aiSessionService.createSession(userId);

    AiSessionResponseDto response =
        new AiSessionResponseDto(
            session.getSessionId(),
            session.getCreatedAt(),
            session.getLastActivity(),
            session.getCurrentVersionId());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      summary = "Delete an AI dashboard session",
      description =
          "Deletes the AI session and its staged data belonging to the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "AI dashboard session deleted successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "AI session does not exist or does not belong to the authenticated user",
            content = @Content)
      })
  @DeleteMapping("/{sessionId}")
  public ResponseEntity<Void> deleteSession(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId) {

    UUID userId = UUID.fromString(jwt.getSubject());

    aiSessionService.deleteSession(userId, sessionId);

    return ResponseEntity.noContent().build();
  }
}
