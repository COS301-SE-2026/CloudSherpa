package com.cloudsherpa.service.analytics.controller;

import com.cloudsherpa.service.analytics.entities.NormalizedMetrics;
import com.cloudsherpa.service.analytics.service.NormalizedMetricService;
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
public class AnalyticsController {

  private final NormalizedMetricService normalizedMetricService;

  AnalyticsController(NormalizedMetricService normalizedMetricService) {
    this.normalizedMetricService = normalizedMetricService;
  }

  @GetMapping("/historical")
  /*
   * Request params
   *   - fromDate: ISO-8601 String, fetch metrics from
   *   - toDate: ISO-8601 String, fetch metrics to
   *  Curl example:
   *   curl "localhost:8083/analytics/historical?from=2026-05-01T10:44:33.000Z&to=2026-05-02T10:44:33.106Z"
   */
  public ResponseEntity<?> getHistoricalData(
      @RequestParam("from") String fromDate, @RequestParam("to") String toDate) {
    try {
      List<NormalizedMetrics> normalizedMetrics =
          normalizedMetricService.fetchHistoricalData(fromDate, toDate);

      if (normalizedMetrics.isEmpty()) {
        Map<String, String> message = new HashMap<>();
        message.put("message", "No metrics for selected window");
        return ResponseEntity.noContent().build();
      }

      return ResponseEntity.ok(normalizedMetrics);
    } catch (Exception e) {
      Map<String, String> message = new HashMap<>();
      message.put("message", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }
}
