package com.cloudsherpa.service.analytics.controller;

import com.cloudsherpa.lib.projections.AggregatedMetric;
import com.cloudsherpa.service.analytics.service.NormalizedMetricService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@Tag(name = "CloudSherpa Analytics", description = "CloudSherpa Analytics endpoints")
public class AnalyticsController {

  private final NormalizedMetricService normalizedMetricService;

  AnalyticsController(NormalizedMetricService normalizedMetricService) {
    this.normalizedMetricService = normalizedMetricService;
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
  /*
   * Request params
   * - fromDate: ISO-8601 String, fetch metrics from
   * - toDate: ISO-8601 String, fetch metrics to
   * - interval: aggregation interval (daily, weekly, monthly)
   * Curl example:
   * curl
   * "localhost:8083/analytics/historical?from=2026-05-01T10:44:33.000Z&to=2026-05-02T10:44:33.106Z&interval=daily"
   */
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
}
