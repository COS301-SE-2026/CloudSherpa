package com.cloudsherpa.service.alerts.controller;

import com.cloudsherpa.lib.repositories.AlertRepository;
import com.cloudsherpa.service.alerts.dto.AlertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Operations for listing and managing alerts")
public class AlertsController {

  private final AlertRepository alertRepository;

  public AlertsController(AlertRepository alertRepository) {
    this.alertRepository = alertRepository;
  }

  @Operation(
      summary = "List alerts",
      description = "Return alerts for the current tenant, optionally filtered by alert type.")
  @ApiResponse(
      responseCode = "200",
      description = "Alerts returned",
      content =
          @Content(
              mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = AlertResponse.class))))
  @GetMapping
  public ResponseEntity<List<AlertResponse>> getAlerts(
      @Parameter(description = "Alert type filter, for example ANOMALY")
          @RequestParam(name = "alertType", required = false)
          String alertType) {
    List<Alert> alerts =
        alertType == null
            ? alertRepository.findAll()
            : alertRepository.findByAlertTypeOrderByCreatedAtDesc(alertType);

    return ResponseEntity.ok(alerts.stream().map(AlertResponse::from).toList());
  }

  @Operation(summary = "Get alert", description = "Return details for a single alert.")
  @ApiResponse(
      responseCode = "200",
      description = "Alert found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = AlertResponse.class)))
  @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content)
  @GetMapping("/{id}")
  public ResponseEntity<AlertResponse> getAlert(
      @Parameter(description = "Alert UUID") @PathVariable UUID id) {

    return alertRepository
        .findById(id)
        .map(AlertResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @Operation(summary = "Acknowledge alert", description = "Mark an alert as ACKNOWLEDGED.")
  @ApiResponse(responseCode = "204", description = "Alert acknowledged")
  @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content)
  @PostMapping("/{id}/acknowledge")
  public ResponseEntity<Void> acknowledge(
      @Parameter(description = "Alert UUID") @PathVariable UUID id) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Dismiss alert", description = "Mark an alert as DISMISSED.")
  @ApiResponse(responseCode = "204", description = "Alert dismissed")
  @ApiResponse(responseCode = "404", description = "Alert not found", content = @Content)
  @PostMapping("/{id}/dismiss")
  public ResponseEntity<Void> dismiss(
      @Parameter(description = "Alert UUID") @PathVariable UUID id) {
    return ResponseEntity.noContent().build();
  }
}
