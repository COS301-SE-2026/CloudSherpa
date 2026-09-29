package com.cloudsherpa.service.agenticdashboard.controller;

import com.cloudsherpa.lib.entities.AiDashboardVersion;
import com.cloudsherpa.service.agenticdashboard.dto.AiVersionResponseDto;
import com.cloudsherpa.service.agenticdashboard.dto.AiVersionSummaryDto;
import com.cloudsherpa.service.agenticdashboard.service.AiDashboardVersionService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/sessions/{sessionId}/versions")
@Tag(name = "AI Dashboard", description = "Agentic Dashboard Construction Operations")
public class AiVersionController {

  private final AiDashboardVersionService versionService;

  public AiVersionController(AiDashboardVersionService versionService) {
    this.versionService = versionService;
  }

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

    UUID userId = UUID.fromString(jwt.getSubject());

    List<AiVersionSummaryDto> response =
        versionService.getVersions(userId, sessionId).stream().map(this::toSummary).toList();

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Activate an AI dashboard version",
      description =
          "Marks the selected dashboard version as active for the AI session and makes it the parent context for the next generated version")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard version activated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AiVersionResponseDto.class))),
        @ApiResponse(
            responseCode = "404",
            description =
                "AI session or dashboard version does not exist or does not belong to the authenticated user",
            content = @Content)
      })
  @PostMapping("/{versionId}/active")
  public ResponseEntity<AiVersionResponseDto> activateVersion(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID sessionId,
      @PathVariable UUID versionId) {

    UUID userId = UUID.fromString(jwt.getSubject());

    return ResponseEntity.ok(versionService.activateVersion(userId, sessionId, versionId));
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

    UUID userId = UUID.fromString(jwt.getSubject());

    return ResponseEntity.ok(versionService.getVersionResponse(userId, sessionId, versionId));
  }

  private AiVersionSummaryDto toSummary(AiDashboardVersion version) {
    return new AiVersionSummaryDto(
        version.getVersionId(),
        version.getVersionNumber(),
        version.getParentVersionId(),
        version.getTitle(),
        version.getCreatedAt(),
        version.getCurrent());
  }
}
