package com.cloudsherpa.service.agenticdashboard.controller;

import com.cloudsherpa.service.agenticdashboard.dto.AiVersionResponseDto;
import com.cloudsherpa.service.agenticdashboard.dto.AiVersionSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/sessions/{sessionId}/versions")
@Tag(name = "AI Dashboard", description = "Agentic Dashboard Construction Operations")
public class AiVersionController {

  @Operation(
      summary = "Get AI dashboard versions",
      description =
          "Returns all staged dashboard versions belonging to the authenticated user's AI session")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard versions retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AiVersionSummaryDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "AI session does not exist or does not belong to the authenticated user",
            content = @Content)
      })
  @GetMapping
  public ResponseEntity<List<AiVersionSummaryDto>> getVersions(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId) {

    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Get an AI dashboard version",
      description =
          "Returns the complete staged dashboard configuration for a specific dashboard version")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard version retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AiVersionResponseDto.class))),
        @ApiResponse(
            responseCode = "404",
            description =
                "Dashboard version does not exist or does not belong to the authenticated user's session",
            content = @Content)
      })
  @GetMapping("/{versionId}")
  public ResponseEntity<AiVersionResponseDto> getVersion(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID sessionId,
      @PathVariable UUID versionId) {

    return ResponseEntity.ok().build();
  }
}
