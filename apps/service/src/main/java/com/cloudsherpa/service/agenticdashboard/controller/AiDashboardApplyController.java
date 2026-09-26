package com.cloudsherpa.service.agenticdashboard.controller;

import com.cloudsherpa.service.agenticdashboard.dto.AiDashboardApplyRequestDto;
import com.cloudsherpa.service.agenticdashboard.service.AiDashboardApplyService;
import com.cloudsherpa.service.dashboard.dto.DashboardDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/sessions/{sessionId}/versions")
@Tag(name = "AI Dashboard", description = "Agentic Dashboard Construction Operations")
public class AiDashboardApplyController {

  private final AiDashboardApplyService applyService;

  public AiDashboardApplyController(AiDashboardApplyService applyService) {
    this.applyService = applyService;
  }

  @Operation(
      summary = "Apply an AI dashboard version",
      description =
          "Applies a staged AI dashboard version by replacing the dashboard the session started on or creating a new dashboard")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard version applied successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DashboardDTO.class, type = "array"))),
        @ApiResponse(
            responseCode = "400",
            description = "The apply request is invalid",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description =
                "AI session, dashboard version, or target dashboard does not exist or does not belong to the authenticated user",
            content = @Content)
      })
  @PostMapping("/{versionId}/apply")
  public ResponseEntity<List<DashboardDTO>> applyVersion(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID sessionId,
      @PathVariable UUID versionId,
      @Valid @RequestBody AiDashboardApplyRequestDto request) {

    UUID userId = UUID.fromString(jwt.getSubject());

    List<DashboardDTO> dashboards =
        applyService.applyVersion(
            userId, sessionId, versionId, request.mode(), request.startedDashboardId());

    return ResponseEntity.ok(dashboards);
  }
}
