package com.cloudsherpa.service.dashboard.controller;

import com.cloudsherpa.service.dashboard.dto.DashboardSaveRequestDTO;
import com.cloudsherpa.service.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@Tag(name = "CloudSherpa Dashboard", description = "CloudSherpa Dashboard endpoints")
public class DashboardController {
  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping
  @Operation(summary = "Get all user dashboards")
  public ResponseEntity<List<DashboardSaveRequestDTO>> getUserDashboards(
      @AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    UUID userId = UUID.fromString(jwt.getSubject());
    List<DashboardSaveRequestDTO> dashboards = dashboardService.getDashboardsByUserId(userId);
    return ResponseEntity.ok(dashboards);
  }

  @DeleteMapping("/{dashboardId}")
  @Operation(summary = "Delete a dashboard that belongs to user")
  public ResponseEntity<Void> deleteDashboard(
      @PathVariable UUID dashboardId, @AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    UUID userId = UUID.fromString(jwt.getSubject());
    dashboardService.deleteDashboard(userId, dashboardId);
    return ResponseEntity.noContent().build();
  }
}
