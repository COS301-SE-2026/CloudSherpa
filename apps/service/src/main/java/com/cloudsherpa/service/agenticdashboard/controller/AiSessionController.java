package com.cloudsherpa.service.agenticdashboard.controller;

import com.cloudsherpa.service.agenticdashboard.dto.AiSessionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/sessions")
@Tag(name = "AI Dashboard", description = "Agentic Dashboard Construction Operations")
public class AiSessionController {

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
  public ResponseEntity<AiSessionResponseDto> createSession() {
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(
      summary = "Delete an AI dashboard session",
      description =
          "Deletes the AI session and all staged dashboard versions and messages belonging to it")
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
  public ResponseEntity<Void> deleteSession(@PathVariable UUID sessionId) {
    return ResponseEntity.noContent().build();
  }
}
