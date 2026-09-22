package com.cloudsherpa.service.alerts.controller;

import com.cloudsherpa.lib.entities.Budget;
import com.cloudsherpa.lib.entities.CurrencyEnum;
import com.cloudsherpa.lib.repositories.BudgetRepository;
import com.cloudsherpa.service.alerts.dto.BudgetResponse;
import com.cloudsherpa.service.alerts.dto.CreateBudgetRequest;
import com.cloudsherpa.service.alerts.dto.UpdateBudgetRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Create and manage billing budgets")
public class BudgetsController {

  private static final List<String> VALID_SCOPES = List.of("TENANT", "ACCOUNT", "RESOURCE");

  private final BudgetRepository budgetRepository;

  public BudgetsController(BudgetRepository budgetRepository) {
    this.budgetRepository = budgetRepository;
  }

  @Operation(
      summary = "Create budget",
      description = "Create a tenant/account/resource scoped budget.")
  @ApiResponse(
      responseCode = "201",
      description = "Budget created",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BudgetResponse.class)))
  @ApiResponse(responseCode = "400", description = "Invalid budget request", content = @Content)
  @PostMapping
  public ResponseEntity<BudgetResponse> createBudget(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "Budget creation request",
              content = @Content(schema = @Schema(implementation = CreateBudgetRequest.class)))
          @RequestBody
          CreateBudgetRequest request) {

    try {
      validateBudget(request.scope(), request.scopeId());
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }

    Budget budget =
        new Budget(
            request.userId(),
            request.scope(),
            request.scopeId(),
            request.amount(),
            request.currency() == null ? CurrencyEnum.USD : request.currency(),
            request.windowDays() == null ? 30 : request.windowDays(),
            request.enabled() == null || request.enabled());

    BudgetResponse response = BudgetResponse.from(budgetRepository.save(budget));

    return ResponseEntity.status(201).body(response);
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
              array = @ArraySchema(schema = @Schema(implementation = BudgetResponse.class))))
  @GetMapping
  public ResponseEntity<List<BudgetResponse>> listBudgets(
      @Parameter(description = "Scope (TENANT|ACCOUNT|RESOURCE)")
          @RequestParam(name = "scope", required = false)
          String scope,
      @Parameter(description = "Scope id (UUID) when scope is ACCOUNT or RESOURCE")
          @RequestParam(name = "scopeId", required = false)
          UUID scopeId) {

    List<Budget> budgets;

    if (scope == null) {
      budgets = budgetRepository.findAll();
    } else {
      budgets = budgetRepository.findByScopeAndScopeId(scope, scopeId);
    }

    List<BudgetResponse> response = new ArrayList<>();

    for (Budget budget : budgets) {
      response.add(BudgetResponse.from(budget));
    }

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update budget", description = "Update an existing budget record.")
  @ApiResponse(responseCode = "200", description = "Budget updated")
  @ApiResponse(responseCode = "400", description = "Invalid budget request", content = @Content)
  @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content)
  @PutMapping("/{id}")
  public ResponseEntity<BudgetResponse> updateBudget(
      @Parameter(description = "Budget UUID") @PathVariable UUID id,
      @RequestBody UpdateBudgetRequest request) {

    Optional<Budget> optionalBudget = budgetRepository.findById(id);

    if (optionalBudget.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Budget budget = optionalBudget.get();

    if (request.amount() != null) {
      budget.setAmount(request.amount());
    }
    if (request.currency() != null) {
      budget.setCurrency(request.currency());
    }
    if (request.windowDays() != null) {
      budget.setWindowDays(request.windowDays());
    }
    if (request.enabled() != null) {
      budget.setEnabled(request.enabled());
    }

    BudgetResponse response = BudgetResponse.from(budgetRepository.save(budget));

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete budget", description = "Delete or disable a budget.")
  @ApiResponse(responseCode = "204", description = "Deleted")
  @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content)
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBudget(
      @Parameter(description = "Budget UUID") @PathVariable UUID id) {

    if (budgetRepository.findById(id).isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    budgetRepository.deleteById(id);

    return ResponseEntity.noContent().build();
  }

  private void validateBudget(String scope, UUID scopeId) {
    if (scope == null || !VALID_SCOPES.contains(scope)) {
      throw new IllegalArgumentException("Unsupported scope: " + scope);
    }

    if (!scope.equals("TENANT") && scopeId == null) {
      throw new IllegalArgumentException("scope_id is required for ACCOUNT/RESOURCE scope");
    }
  }
}
