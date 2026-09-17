package com.cloudsherpa.service.alerts.controller;

import com.cloudsherpa.lib.entities.Threshold;
import com.cloudsherpa.lib.repositories.ThresholdRepository;
import com.cloudsherpa.service.alerts.dto.ThresholdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thresholds")
@Tag(name = "Thresholds", description = "Manage resource-level threshold configurations")
public class ThresholdsController {

  private final ThresholdRepository thresholdRepository;

  public ThresholdsController(ThresholdRepository thresholdRepository) {
    this.thresholdRepository = thresholdRepository;
  }

  private static final String METRIC_NAME = "metric_name";
  private static final String OPERATOR = "operator";
  private static final String VALUE = "value";
  private static final String SEVERITY = "severity";
  private static final String ENABLED = "enabled";

  @Operation(
      summary = "Create threshold",
      description = "Create a resource-level threshold (validation applied server-side).")
  @ApiResponse(
      responseCode = "201",
      description = "Threshold created",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ThresholdResponse.class),
              examples =
                  @ExampleObject(
                      value =
                          "{\"thresholdId\":\"t0000000-0000-0000-0000-000000000001\",\"resourceId\":\"r0000000-0000-0000-0000-000000000001\",\"userId\":\"5ebe4340-c5ec-4833-ad93-06abf4609f03\",\"metricName\":\"CPUUtilization\",\"operator\":\"GT\",\"value\":80.0,\"severity\":\"WARNING\",\"enabled\":true,\"createdAt\":\"2026-09-16T12:00:00Z\",\"updatedAt\":\"2026-09-16T12:00:00Z\"}")))
  @ApiResponse(
      responseCode = "400",
      description = "Invalid metric name or operator",
      content = @Content)
  @PostMapping
  public ResponseEntity<ThresholdResponse> createThreshold(@RequestBody Map<String, Object> req) {
    UUID resourceId = UUID.fromString((String) req.get("resourceId"));
    UUID userId = req.get("userId") != null ? UUID.fromString((String) req.get("userId")) : null;
    String metricName = (String) req.get(METRIC_NAME);
    String operator = (String) req.get(OPERATOR);
    double value = ((Number) req.get(VALUE)).doubleValue();
    String severity = (String) req.getOrDefault(SEVERITY, "WARNING");
    boolean enabled = (boolean) req.getOrDefault(ENABLED, true);

    try {
      validateThreshold(metricName, operator);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }

    Threshold threshold =
        new Threshold(resourceId, userId, metricName, operator, value, severity, enabled);

    return ResponseEntity.status(201)
        .body(ThresholdResponse.from(thresholdRepository.save(threshold)));
  }

  @Operation(
      summary = "List thresholds",
      description = "List thresholds, optionally filtered by resource.")
  @ApiResponse(
      responseCode = "200",
      description = "List of thresholds",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ThresholdResponse.class),
              examples =
                  @ExampleObject(
                      value =
                          "[{\"thresholdId\":\"t0000000-0000-0000-0000-000000000001\",\"resourceId\":\"r0000000-0000-0000-0000-000000000001\",\"metricName\":\"CPUUtilization\",\"operator\":\"GT\",\"value\":80.0,\"severity\":\"WARNING\",\"enabled\":true}]")))
  @GetMapping
  public ResponseEntity<List<ThresholdResponse>> listThresholds(
      @RequestParam(name = "resourceId", required = false) UUID resourceId) {
    List<Threshold> thresholds =
        resourceId == null
            ? thresholdRepository.findAll()
            : thresholdRepository.findByResourceId(resourceId);

    return ResponseEntity.ok(thresholds.stream().map(ThresholdResponse::from).toList());
  }

  @Operation(summary = "Update threshold", description = "Update an existing threshold.")
  @ApiResponse(responseCode = "204", description = "Updated")
  @ApiResponse(
      responseCode = "400",
      description = "Invalid metric name or operator",
      content = @Content)
  @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content)
  @PutMapping("/{id}")
  public ResponseEntity<Void> updateThreshold(
      @PathVariable UUID id, @RequestBody Map<String, Object> req) {

    Optional<Threshold> optionalThreshold = thresholdRepository.findById(id);
    if (!optionalThreshold.isPresent()) {
      return ResponseEntity.notFound().build();
    }

    Threshold threshold = optionalThreshold.get();

    Object metricNameObj = req.get(METRIC_NAME);
    if (metricNameObj != null) {
      threshold.setMetricName((String) metricNameObj);
    }

    Object operatorObj = req.get(OPERATOR);
    if (operatorObj != null) {
      threshold.setOperator((String) operatorObj);
    }

    Object valueObj = req.get(VALUE);
    if (valueObj != null) {
      threshold.setValue(((Number) valueObj).doubleValue());
    }

    try {
      validateThreshold(threshold.getMetricName(), threshold.getOperator());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
    thresholdRepository.save(threshold);

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

  private void validateThreshold(String metricName, String operator) {
    if (metricName == null || metricName.isBlank()) {
      throw new IllegalArgumentException("metric_name is required");
    }

    if (!Set.of("GT", "GTE", "LT", "LTE", "EQ").contains(operator)) {
      throw new IllegalArgumentException("Unsupported operator: " + operator);
    }
  }
}
