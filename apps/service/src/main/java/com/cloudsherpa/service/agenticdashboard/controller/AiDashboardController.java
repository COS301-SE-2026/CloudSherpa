package com.cloudsherpa.service.agenticdashboard.controller;

import com.cloudsherpa.service.agenticdashboard.dto.AiDashboardPlanRequestDto;
import com.cloudsherpa.service.agenticdashboard.dto.AiDashboardPlanResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
            description = "The dashboard request is malformed or contains invalid values",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description =
                "The AI session does not exist or does not belong to the authenticated user",
            content = @Content),
        @ApiResponse(
            responseCode = "422",
            description =
                "The requested dashboard cannot be represented using the available CloudSherpa dashboard capabilities",
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

    return ResponseEntity.ok().build();
  }
}
