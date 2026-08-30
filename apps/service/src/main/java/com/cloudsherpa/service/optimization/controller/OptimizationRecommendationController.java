package com.cloudsherpa.service.optimization.controller;

import com.cloudsherpa.service.optimization.service.OptimizationRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/optimization/recommendations")
@Tag(
    name = "Optimization Recommendations",
    description = "Retrieve and manage cloud optimization recommendations")
public class OptimizationRecommendationController {

  private final OptimizationRecommendationService service;

  public OptimizationRecommendationController(OptimizationRecommendationService service) {
    this.service = service;
  }

  @GetMapping
  @Operation(
      summary = "List optimization recommendations",
      description = "Returns optimization recommendations matching the supplied filters.")
  public List<Map<String, Object>> getRecommendations(
      @Parameter(description = "Recommendation lifecycle status", example = "ACTIVE")
          @RequestParam(required = false)
          String status,
      @Parameter(description = "Cloud provider", example = "AWS") @RequestParam(required = false)
          String provider,
      @Parameter(description = "Cloud resource type", example = "compute_instance")
          @RequestParam(required = false)
          String resourceType,
      @Parameter(description = "Recommended action type", example = "DOWNSIZE")
          @RequestParam(required = false)
          String actionType,
      @Parameter(description = "Resource UUID") @RequestParam(required = false) UUID resourceId) {

    // use with parameters: status, provider, resourceType, actionType, resourceId
    return service.getRecommendations();
  }

  @GetMapping("/summary")
  @Operation(
      summary = "Get recommendation summary",
      description = "Returns recommendation counts grouped by status and action type.")
  public Map<String, Object> getRecommendationSummary() {
    return service.getRecommendationSummary();
  }

  @GetMapping("/{recommendationId}")
  @Operation(
      summary = "Get an optimization recommendation",
      description = "Returns a single optimization recommendation by its identifier.")
  public Map<String, Object> getRecommendation(
      @Parameter(description = "Recommendation UUID", required = true) @PathVariable
          UUID recommendationId) {

    return service.getRecommendation(recommendationId);
  }

  @PostMapping("/{recommendationId}/acknowledge")
  @Operation(
      summary = "Acknowledge a recommendation",
      description = "Marks an active recommendation as acknowledged.")
  public Map<String, Object> acknowledgeRecommendation(
      @Parameter(description = "Recommendation UUID", required = true) @PathVariable
          UUID recommendationId) {

    return service.acknowledgeRecommendation(recommendationId);
  }

  @PostMapping("/{recommendationId}/dismiss")
  @Operation(
      summary = "Dismiss a recommendation",
      description = "Marks an active recommendation as dismissed.")
  public Map<String, Object> dismissRecommendation(
      @Parameter(description = "Recommendation UUID", required = true) @PathVariable
          UUID recommendationId) {

    return service.dismissRecommendation(recommendationId);
  }

  @PostMapping("/{recommendationId}/apply")
  @Operation(
      summary = "Apply a recommendation",
      description = "Marks an active recommendation as applied.")
  public Map<String, Object> applyRecommendation(
      @Parameter(description = "Recommendation UUID", required = true) @PathVariable
          UUID recommendationId) {

    return service.applyRecommendation(recommendationId);
  }

  @PostMapping("/{recommendationId}/re-enable")
  public Map<String, Object> reEnableRecommendation(@PathVariable UUID recommendationId) {
    return service.reEnableRecommendation(recommendationId);
  }
}
