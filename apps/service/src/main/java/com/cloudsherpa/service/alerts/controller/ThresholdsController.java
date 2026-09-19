package com.cloudsherpa.service.alerts.controller;

import com.cloudsherpa.lib.entities.Threshold;
import com.cloudsherpa.lib.repositories.ThresholdRepository;
import com.cloudsherpa.service.alerts.dto.CreateThresholdRequest;
import com.cloudsherpa.service.alerts.dto.ThresholdResponse;
import com.cloudsherpa.service.alerts.dto.UpdateThresholdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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

  @Operation(summary = "Create threshold", description = "Create a resource-level threshold.")
  @ApiResponse(
      responseCode = "201",
      description = "Threshold created",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ThresholdResponse.class)))
  @ApiResponse(responseCode = "400", description = "Invalid threshold request", content = @Content)
  @PostMapping
  public ResponseEntity<ThresholdResponse> createThreshold(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "Threshold creation request",
              content = @Content(schema = @Schema(implementation = CreateThresholdRequest.class)))
          @RequestBody
          CreateThresholdRequest request) {

    try {
      validateThreshold(request.metricName(), request.operator());
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }

    Threshold threshold =
        new Threshold(
            request.resourceId(),
            request.userId(),
            request.metricName(),
            request.operator(),
            request.value(),
            request.severity() == null ? "WARNING" : request.severity(),
            request.enabled() == null || request.enabled());

    ThresholdResponse response = ThresholdResponse.from(thresholdRepository.save(threshold));

    return ResponseEntity.status(201).body(response);
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
              array = @ArraySchema(schema = @Schema(implementation = ThresholdResponse.class))))
  @GetMapping
  public ResponseEntity<List<ThresholdResponse>> listThresholds(
      @Parameter(description = "Resource UUID to filter thresholds")
          @RequestParam(name = "resourceId", required = false)
          UUID resourceId) {

    List<Threshold> thresholds =
        resourceId == null
            ? thresholdRepository.findAll()
            : thresholdRepository.findByResourceId(resourceId);

    return ResponseEntity.ok(thresholds.stream().map(ThresholdResponse::from).toList());
  }

  @Operation(summary = "Update threshold", description = "Update an existing threshold.")
  @ApiResponse(responseCode = "204", description = "Threshold updated")
  @ApiResponse(responseCode = "400", description = "Invalid threshold request", content = @Content)
  @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content)
  @PutMapping("/{id}")
  public ResponseEntity<Void> updateThreshold(
      @Parameter(description = "Threshold UUID") @PathVariable UUID id,
      @RequestBody UpdateThresholdRequest request) {

    try {
      if (request.metricName() != null && request.metricName().isBlank()) {
        throw new IllegalArgumentException("metric_name is required");
      }

      if (request.operator() != null
          && !Set.of("GT", "GTE", "LT", "LTE", "EQ").contains(request.operator())) {
        throw new IllegalArgumentException("Unsupported operator: " + request.operator());
      }

      if (request.value() != null && !Double.isFinite(request.value())) {
        throw new IllegalArgumentException("value must be a finite number");
      }
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }

    Optional<Threshold> optionalThreshold = thresholdRepository.findById(id);

    if (optionalThreshold.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Threshold threshold = optionalThreshold.get();

    String metricName =
        request.metricName() != null ? request.metricName() : threshold.getMetricName();

    String operator = request.operator() != null ? request.operator() : threshold.getOperator();

    double value = request.value() != null ? request.value() : threshold.getValue();

    try {
      validateThreshold(metricName, operator);
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }

    threshold.setMetricName(metricName);
    threshold.setOperator(operator);
    threshold.setValue(value);

    if (request.severity() != null) {
      threshold.setSeverity(request.severity());
    }

    if (request.enabled() != null) {
      threshold.setEnabled(request.enabled());
    }

    thresholdRepository.save(threshold);

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Delete threshold", description = "Delete an existing threshold.")
  @ApiResponse(responseCode = "204", description = "Threshold deleted")
  @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content)
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteThreshold(
      @Parameter(description = "Threshold UUID") @PathVariable UUID id) {

    if (!thresholdRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }

    thresholdRepository.deleteById(id);
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
