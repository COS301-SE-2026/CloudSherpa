package com.cloudsherpa.service.alerts.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
public class BudgetsController {

  @PostMapping
  public ResponseEntity<Map<String, Object>> createBudget(@RequestBody Map<String, Object> req) {
    return ResponseEntity.ok(Map.of("budgetId", UUID.randomUUID()));
  }

  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> listBudgets(
      @RequestParam(name = "scope", required = false) String scope,
      @RequestParam(name = "scopeId", required = false) UUID scopeId) {
    // return budgets for tenant
    return ResponseEntity.ok(List.of());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> updateBudget(
      @PathVariable UUID id, @RequestBody Map<String, Object> req) {
    // update budget row
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBudget(@PathVariable UUID id) {
    // delete or disable budget
    return ResponseEntity.noContent().build();
  }
}
