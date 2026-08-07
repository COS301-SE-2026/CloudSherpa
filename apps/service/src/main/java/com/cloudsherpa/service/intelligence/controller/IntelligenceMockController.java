package com.cloudsherpa.service.intelligence.controller;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.service.intelligence.service.Sampler;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/intelligence/mock")
public class IntelligenceMockController {

  private final Sampler sampler;

  public IntelligenceMockController(Sampler sampler) {
    this.sampler = sampler;
  }

  @GetMapping("/usage-sample")
  public ResponseEntity<List<TimestampedNumericDataPoint>> sampleUsage() {
    List<TimestampedNumericDataPoint> mockSeries =
        List.of(
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(10.0), Instant.parse("2026-08-03T00:00:00Z")),
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(12.10), Instant.parse("2026-08-03T08:00:00Z")),
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(14.25), Instant.parse("2026-08-03T09:00:00Z")),
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(13.80), Instant.parse("2026-08-03T10:00:00Z")),
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(16.40), Instant.parse("2026-08-03T11:00:00Z")),
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(15.95), Instant.parse("2026-08-03T12:00:00Z")));

    return ResponseEntity.status(HttpStatus.OK).body(sampler.sample(mockSeries, true));
  }
}
