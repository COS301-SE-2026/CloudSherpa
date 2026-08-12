package com.cloudsherpa.service.analytics.controller;

import com.cloudsherpa.lib.projections.AggregatedMetric;
import com.cloudsherpa.service.analytics.dto.ResourceMetricHistoricalRequestDto;
import com.cloudsherpa.service.analytics.dto.ResourceMetricHistoricalResponseDto;
import com.cloudsherpa.service.analytics.dto.ResourceNameDto;
import com.cloudsherpa.service.analytics.service.NormalizedMetricService;
import com.cloudsherpa.service.analytics.service.ResourceRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@Tag(name = "CloudSherpa Analytics", description = "CloudSherpa Analytics endpoints")
public class AnalyticsController {

  private final NormalizedMetricService normalizedMetricService;
  private final ResourceRegistryService resourceRegistryService;

  AnalyticsController(
      NormalizedMetricService normalizedMetricService,
      ResourceRegistryService resourceRegistryService) {
    this.normalizedMetricService = normalizedMetricService;
    this.resourceRegistryService = resourceRegistryService;
  }

  @Operation(summary = "Get authoratative metric data")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "No metrics found for selected time window"),
        @ApiResponse(
            responseCode = "200",
            description = "Return metrics found",
            content =
                @Content(
                    array =
                        @ArraySchema(schema = @Schema(implementation = AggregatedMetric.class))))
      })
  @GetMapping("/historical")
  public ResponseEntity<List<AggregatedMetric>> getHistoricalData(
      @RequestParam("from") String fromDate,
      @RequestParam("to") String toDate,
      @RequestParam(name = "interval", defaultValue = "daily") String interval) {
    try {
      List<AggregatedMetric> aggregatedMetrics =
          normalizedMetricService.fetchHistoricalData(fromDate, toDate, interval);

      if (aggregatedMetrics.isEmpty()) {
        Map<String, String> message = new HashMap<>();
        message.put("message", "No metrics for selected window");
        return ResponseEntity.noContent().build();
      }

      return ResponseEntity.ok(aggregatedMetrics);
    } catch (Exception e) {
      Map<String, String> message = new HashMap<>();
      message.put("message", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/resource-names")
  public List<ResourceNameDto> getResourceNames(JwtAuthenticationToken authentication) {

    Jwt jwt = authentication.getToken();
    UUID userId = UUID.fromString(jwt.getSubject());

    return resourceRegistryService.getResourceNamesByUserId(userId);
  }

  @Operation(summary = "Get historical metric data for a resource")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Return metric values and timestamps for the resource",
            content =
                @Content(
                    schema = @Schema(implementation = ResourceMetricHistoricalResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "No resource metrics found")
      })
  @PostMapping("/historical-resource-metric")
  public ResponseEntity<ResourceMetricHistoricalResponseDto> postMethodName(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Resource metric query parameters",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = ResourceMetricHistoricalRequestDto.class)))
          @RequestBody
          ResourceMetricHistoricalRequestDto request) {

    return ResponseEntity.ok()
        .body(
            normalizedMetricService.fetchHistoricalDataForResourceMetric(
                request.resourceId(), request.metricType(), request.fromDate()));
  }
}
