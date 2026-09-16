package com.cloudsherpa.service.alerts.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertsController {

  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> getAlerts() {
    // fetch all alerts
    return ResponseEntity.ok(List.of());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getAlert(@PathVariable UUID id) {
    // fetch alert details
    return ResponseEntity.ok(Map.of());
  }

  @PostMapping("/{id}/acknowledge")
  public ResponseEntity<Void> acknowledge(@PathVariable UUID id) {
    // update status -> ACKNOWLEDGED
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/dismiss")
  public ResponseEntity<Void> dismiss(@PathVariable UUID id) {
    // update status -> DISMISSED
    return ResponseEntity.noContent().build();
  }
}
