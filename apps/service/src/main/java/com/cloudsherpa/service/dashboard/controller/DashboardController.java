package com.cloudsherpa.service.dashboard.controller;

import com.cloudsherpa.service.dashboard.dto.DashboardCreateDTO;
import com.cloudsherpa.service.dashboard.dto.DashboardDTO;
import com.cloudsherpa.service.dashboard.dto.WidgetConfigUpdateDTO;
import com.cloudsherpa.service.dashboard.dto.WidgetDTO;
import com.cloudsherpa.service.dashboard.dto.WidgetLayoutUpdateDTO;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboards")
@Tag(name = "CloudSherpa Dashboard", description = "CloudSherpa Dashboard endpoints")
public class DashboardController {
  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping
  @Operation(summary = "Get all user dashboards with their widgets")
  public ResponseEntity<List<DashboardDTO>> getUserDashboards(@AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());

    return ResponseEntity.ok(dashboardService.getDashboardsByUserId(userId));
  }

  @PostMapping
  @Operation(summary = "Create a new blank dashboard")
  public ResponseEntity<DashboardDTO> createDashboard(
      @RequestBody DashboardCreateDTO request, @AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());
    DashboardCreateDTO requestWithUser = request.withUserId(userId);
    return ResponseEntity.ok(dashboardService.createDashboard(requestWithUser));
  }

  @DeleteMapping("/{dashboardId}")
  @Operation(summary = "Delete a dashboard")
  public ResponseEntity<Void> deleteDashboard(
      @PathVariable UUID dashboardId, @AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());

    dashboardService.deleteDashboard(userId, dashboardId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{dashboardId}/layout")
  @Operation(summary = "Batch update the sizes and positions of all widgets on a dashboard")
  public ResponseEntity<Void> updateDashboardLayout(
      @PathVariable UUID dashboardId,
      @RequestBody List<WidgetLayoutUpdateDTO> layouts,
      @AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());

    dashboardService.updateDashboardLayout(userId, dashboardId, layouts);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{dashboardId}/widgets")
  @Operation(summary = "Add a new widget to a dashboard")
  public ResponseEntity<Void> createChartWidget(
      @PathVariable UUID dashboardId,
      @RequestBody WidgetDTO request,
      @AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{dashboardId}/widgets/kpi")
  public void createKpiWidget(@PathVariable UUID dashboardId) {
    // To be implemented
  }

  @PatchMapping("/widgets/{widgetId}/config")
  @Operation(summary = "Update a widget's visual or data configuration")
  public ResponseEntity<WidgetDTO> updateWidgetConfig(
      @PathVariable UUID widgetId,
      @RequestBody WidgetConfigUpdateDTO request,
      @AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());

    WidgetConfigUpdateDTO requestWithUser = request.withUserId(userId);
    return ResponseEntity.ok(dashboardService.updateWidgetConfig(widgetId, requestWithUser));
  }

  @DeleteMapping("/widgets/{widgetId}")
  @Operation(summary = "Delete a widget from a dashboard")
  public ResponseEntity<Void> deleteWidget(
      @PathVariable UUID widgetId, @AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());

    dashboardService.deleteWidget(userId, widgetId);
    return ResponseEntity.noContent().build();
  }
}
