package com.cloudsherpa.service.agenticdashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/sessions/{sessionId}/versions")
@Tag(name = "AI Dashboard", description = "Agentic Dashboard Construction Operations")
public class AiDashboardApplyController {

  @Operation(
      summary = "Apply an AI dashboard version",
      description =
          "Creates a real CloudSherpa dashboard from a staged AI dashboard version selected by the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Dashboard version applied successfully",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description =
                "AI session or dashboard version does not exist or does not belong to the authenticated user",
            content = @Content),
        @ApiResponse(
            responseCode = "422",
            description = "The dashboard version is no longer valid and cannot be applied",
            content = @Content)
      })
  @PostMapping("/{versionId}/apply")
  public ResponseEntity<Void> applyVersion(
      @PathVariable UUID sessionId, @PathVariable UUID versionId) {

    return ResponseEntity.status(201).build();
  }
}
