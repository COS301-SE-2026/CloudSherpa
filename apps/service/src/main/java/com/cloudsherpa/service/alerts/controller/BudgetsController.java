package com.cloudsherpa.service.alerts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Create and manage billing budgets")
public class BudgetsController {

  @Operation(
      summary = "Create budget",
      description = "Create a tenant/account/resource scoped budget.")
  @ApiResponse(
      responseCode = "201",
      description = "Budget created",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = Map.class),
              examples =
                  @ExampleObject(
                      value =
                          "{\"budgetId\":\"b0000000-0000-0000-0000-000000000002\",\"scope\":\"TENANT\",\"scopeId\":null,\"amount\":1000.0,\"currency\":\"USD\",\"window_days\":30,\"enabled\":true}")))
  @PostMapping
  public ResponseEntity<Map<String, Object>> createBudget(
      @Parameter(description = "Budget creation payload") @RequestBody Map<String, Object> req) {
    return ResponseEntity.ok(Map.of("budgetId", UUID.randomUUID()));
  }

  @Operation(
      summary = "List budgets",
      description = "List budgets for the current tenant; optional scope filter.")
  @ApiResponse(
      responseCode = "200",
      description = "List of budgets",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = List.class),
              examples =
                  @ExampleObject(
                      value =
                          "[{\"budgetId\":\"b0000000-0000-0000-0000-000000000002\",\"scope\":\"TENANT\",\"scopeId\":null,\"amount\":1000.0,\"currency\":\"USD\",\"window_days\":30,\"enabled\":true}]")))
  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> listBudgets(
      @Parameter(description = "Scope (TENANT|ACCOUNT|RESOURCE)")
          @RequestParam(name = "scope", required = false)
          String scope,
      @Parameter(description = "Scope id (UUID) when scope is ACCOUNT or RESOURCE")
          @RequestParam(name = "scopeId", required = false)
          UUID scopeId) {
    // return budgets for tenant
    return ResponseEntity.ok(List.of());
  }

  @Operation(summary = "Update budget", description = "Update an existing budget record.")
  @ApiResponse(responseCode = "204", description = "Updated")
  @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content)
  @PutMapping("/{id}")
  public ResponseEntity<Void> updateBudget(
      @Parameter(description = "Budget UUID") @PathVariable UUID id,
      @Parameter(description = "Budget update payload") @RequestBody Map<String, Object> req) {
    // update budget row
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Delete budget", description = "Delete or disable a budget.")
  @ApiResponse(responseCode = "204", description = "Deleted/disabled")
  @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content)
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBudget(
      @Parameter(description = "Budget UUID") @PathVariable UUID id) {
    // delete or disable budget
    return ResponseEntity.noContent().build();
  }
}
