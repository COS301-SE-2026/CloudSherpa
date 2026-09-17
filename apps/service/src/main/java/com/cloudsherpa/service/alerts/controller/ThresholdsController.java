package com.cloudsherpa.service.alerts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thresholds")
@Tag(name = "Thresholds", description = "Manage widget-level threshold configurations")
public class ThresholdsController {

  private static final String WIDGET_ID = "widgetId";
  private static final String METRIC_NAME = "metric_name";
  private static final String OPERATOR = "operator";
  private static final String VALUE = "value";
  private static final String SEVERITY = "severity";
  private static final String ENABLED = "enabled";

  @Operation(
      summary = "Create threshold",
      description =
          "Create a widget-level threshold (only widget owners allowed; validation applied server-side).")
  @ApiResponse(
      responseCode = "201",
      description = "Threshold created",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = Map.class),
              examples =
                  @ExampleObject(
                      value =
                          "{\"thresholdId\":\"t0000000-0000-0000-0000-000000000001\",\"widgetId\":\"w0000000-0000-0000-0000-000000000001\",\"userId\":\"5ebe4340-c5ec-4833-ad93-06abf4609f03\",\"metricName\":\"CPUUtilization\",\"operator\":\"GT\",\"value\":80.0,\"severity\":\"WARNING\",\"enabled\":true,\"createdAt\":\"2026-09-16T12:00:00Z\",\"updatedAt\":\"2026-09-16T12:00:00Z\"}")))
  @PostMapping
  public ResponseEntity<Map<String, Object>> createThreshold(
      @Parameter(description = "Threshold creation payload") @RequestBody Map<String, Object> req) {
    // Example response
    Map<String, Object> created =
        Map.of(
            "thresholdId",
            UUID.randomUUID().toString(),
            WIDGET_ID,
            req.getOrDefault(WIDGET_ID, null),
            "userId",
            req.getOrDefault("userId", null),
            METRIC_NAME,
            req.getOrDefault(METRIC_NAME, "CPUUtilization"),
            OPERATOR,
            req.getOrDefault(OPERATOR, "GT"),
            VALUE,
            req.getOrDefault(VALUE, 80.0),
            SEVERITY,
            req.getOrDefault(SEVERITY, "WARNING"),
            ENABLED,
            req.getOrDefault(ENABLED, true),
            "createdAt",
            OffsetDateTime.now(ZoneOffset.UTC).toString(),
            "updatedAt",
            OffsetDateTime.now(ZoneOffset.UTC).toString());
    return ResponseEntity.status(201).body(created);
  }

  @Operation(
      summary = "List thresholds",
      description = "List thresholds for a widget (scoped to tenant via global security).")
  @ApiResponse(
      responseCode = "200",
      description = "List of thresholds",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = List.class),
              examples =
                  @ExampleObject(
                      value =
                          "[{\"thresholdId\":\"t0000000-0000-0000-0000-000000000001\",\"widgetId\":\"w0000000-0000-0000-0000-000000000001\",\"metricName\":\"CPUUtilization\",\"operator\":\"GT\",\"value\":80.0,\"severity\":\"WARNING\",\"enabled\":true}]")))
  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> listThresholds(
      @Parameter(description = "Widget UUID to filter thresholds")
          @RequestParam(name = WIDGET_ID, required = false)
          UUID widgetId) {
    // Example-only response
    Map<String, Object> sample =
        Map.of(
            "thresholdId",
            "t0000000-0000-0000-0000-000000000001",
            WIDGET_ID,
            widgetId == null ? "w0000000-0000-0000-0000-000000000001" : widgetId.toString(),
            METRIC_NAME,
            "CPUUtilization",
            OPERATOR,
            "GT",
            VALUE,
            80.0,
            SEVERITY,
            "WARNING",
            ENABLED,
            true,
            "createdAt",
            "2026-09-16T12:00:00Z",
            "updatedAt",
            "2026-09-16T12:00:00Z");
    return ResponseEntity.ok(List.of(sample));
  }

  @Operation(
      summary = "Update threshold",
      description = "Update an existing threshold (widget ownership enforced server-side).")
  @ApiResponse(responseCode = "204", description = "Updated")
  @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content)
  @PutMapping("/{id}")
  public ResponseEntity<Void> updateThreshold(
      @Parameter(description = "Threshold UUID") @PathVariable UUID id,
      @Parameter(description = "Threshold update payload") @RequestBody Map<String, Object> req) {
    // persist update
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Delete threshold", description = "Delete or disable a threshold.")
  @ApiResponse(responseCode = "204", description = "Deleted/disabled")
  @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content)
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteThreshold(
      @Parameter(description = "Threshold UUID") @PathVariable UUID id) {
    // delete or disable
    return ResponseEntity.noContent().build();
  }
}
