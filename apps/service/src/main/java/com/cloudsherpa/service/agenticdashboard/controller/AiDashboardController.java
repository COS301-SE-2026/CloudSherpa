package com.cloudsherpa.service.agenticdashboard.controller;

import com.cloudsherpa.service.agenticdashboard.dto.AiDashboardPlanRequestDto;
import com.cloudsherpa.service.agenticdashboard.dto.AiDashboardPlanResponseDto;
import com.cloudsherpa.service.agenticdashboard.service.AiAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/dashboard")
@Tag(name = "AI Dashboard", description = "Agentic Dashboard Construction Operations")
public class AiDashboardController {

  private final AiAgentService aiAgentService;

  public AiDashboardController(AiAgentService aiAgentService) {
    this.aiAgentService = aiAgentService;
  }

  @Operation(
      summary = "Generate an AI dashboard plan",
      description =
          "Processes a natural-language dashboard request and creates a new staged dashboard version")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard plan generated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AiDashboardPlanResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "The dashboard request is malformed",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description =
                "The AI session does not exist or does not belong to the authenticated user",
            content = @Content)
      })
  @PostMapping("/plan")
  public ResponseEntity<AiDashboardPlanResponseDto> createDashboardPlan(
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody(
              description =
                  "Natural-language dashboard instruction and the AI session in which it should be processed",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = AiDashboardPlanRequestDto.class)))
          @Valid
          @org.springframework.web.bind.annotation.RequestBody
          AiDashboardPlanRequestDto request) {

    UUID userId = UUID.fromString(jwt.getSubject());

    AiDashboardPlanResponseDto response =
        aiAgentService.generateDashboardPlan(userId, request.sessionId(), request.message());

    return ResponseEntity.ok(response);
  }
}
