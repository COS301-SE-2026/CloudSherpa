package com.cloudsherpa.service.billing.controller;

import com.cloudsherpa.service.billing.dto.BillingConnectionResponse;
import com.cloudsherpa.service.billing.dto.BillingKpiRequest;
import com.cloudsherpa.service.billing.dto.BillingKpiResponse;
import com.cloudsherpa.service.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing")
@Tag(name = "Billing", description = "Billing KPI endpoints")
public class BillingController {

  private final BillingService billingService;

  public BillingController(BillingService billingService) {
    this.billingService = billingService;
  }

  @Operation(
      summary = "Preview billing KPI card",
      description =
          "Returns a preview value for a KPI card using selected resources, date range, and aggregation")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "KPI preview generated",
            content = @Content(schema = @Schema(implementation = BillingKpiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request")
      })
  @PostMapping("/kpis/preview")
  public ResponseEntity<BillingKpiResponse> previewKpi(@RequestBody BillingKpiRequest request) {

    BillingKpiResponse response = billingService.previewKpi(request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get billing connections")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Connections returned successfully",
            content = @Content(schema = @Schema(implementation = BillingConnectionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated")
      })
  @GetMapping("/connections")
  public ResponseEntity<List<BillingConnectionResponse>> getConnections(
      @AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID userId = UUID.fromString(jwt.getSubject());
    return ResponseEntity.ok(billingService.getConnections(userId));
  }
}
