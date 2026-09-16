package com.cloudsherpa.service.alerts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Operations for listing and managing alerts")
public class AlertsController {

  @Operation(
      summary = "List alerts",
      description =
          "Return a list of alerts for the current tenant/user (security/tenant scoping handled globally).")
  @ApiResponse(
      responseCode = "200",
      description = "A list of alerts",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = Map.class),
              examples =
                  @ExampleObject(
                      value =
                          "[{\"alert_id\":\"b0000000-0000-0000-0000-000000000001\",\"alert_type\":\"BILLING\",\"severity\":\"WARNING\",\"title\":\"30-day projected spend may exceed budget\",\"message\":\"Projected 30d spend $1,200 vs budget $1,000\",\"status\":\"ACTIVE\",\"created_at\":\"2026-09-16T12:00:00Z\"}]")))
  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> getAlerts() {
    // fetch all alerts
    return ResponseEntity.ok(List.of());
  }

  @Operation(summary = "Get alert", description = "Return details for a single alert by id.")
  @ApiResponse(
      responseCode = "200",
      description = "Alert found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = Map.class),
              examples =
                  @ExampleObject(
                      value =
                          "{\"alert_id\":\"b0000000-0000-0000-0000-000000000001\",\"alert_type\":\"THRESHOLD\",\"severity\":\"CRITICAL\",\"title\":\"CPU > 90%\",\"message\":\"CPU usage 95% on instance i-0123\",\"status\":\"ACTIVE\",\"created_at\":\"2026-09-16T12:00:00Z\"}")))
  @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content)
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getAlert(
      @Parameter(description = "Alert UUID") @PathVariable UUID id) {
    // fetch alert details
    return ResponseEntity.ok(Map.of());
  }

  @Operation(summary = "Acknowledge alert", description = "Mark an alert as ACKNOWLEDGED.")
  @ApiResponse(responseCode = "204", description = "Alert acknowledged")
  @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content)
  @PostMapping("/{id}/acknowledge")
  public ResponseEntity<Void> acknowledge(
      @Parameter(description = "Alert UUID") @PathVariable UUID id) {
    // update status -> ACKNOWLEDGED
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Dismiss alert", description = "Mark an alert as DISMISSED.")
  @ApiResponse(responseCode = "204", description = "Alert dismissed")
  @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content)
  @PostMapping("/{id}/dismiss")
  public ResponseEntity<Void> dismiss(
      @Parameter(description = "Alert UUID") @PathVariable UUID id) {
    // update status -> DISMISSED
    return ResponseEntity.noContent().build();
  }
}
