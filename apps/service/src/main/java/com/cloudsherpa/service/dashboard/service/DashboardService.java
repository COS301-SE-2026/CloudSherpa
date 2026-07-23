package com.cloudsherpa.service.dashboard.service;

import com.cloudsherpa.lib.entities.Dashboard;
import com.cloudsherpa.lib.entities.Widget;
import com.cloudsherpa.lib.repositories.DashboardRepository;
import com.cloudsherpa.lib.repositories.DashboardWidgetRepository;
import com.cloudsherpa.service.dashboard.dto.ChartWidgetConfigUpdateDTO;
import com.cloudsherpa.service.dashboard.dto.ChartWidgetDTO;
import com.cloudsherpa.service.dashboard.dto.DashboardCreateDTO;
import com.cloudsherpa.service.dashboard.dto.DashboardDTO;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetConfigUpdateDTO;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetDTO;
import com.cloudsherpa.service.dashboard.dto.WidgetConfigUpdateDTO;
import com.cloudsherpa.service.dashboard.dto.WidgetDTO;
import com.cloudsherpa.service.dashboard.dto.WidgetLayoutUpdateDTO;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardService {
  private final DashboardRepository dashboardRepository;
  private final DashboardWidgetRepository widgetRepository;
  private final ChartWidgetService chartWidgetService;
  private final KpiWidgetService kpiWidgetService;

  public DashboardService(
      DashboardRepository dashboardRepository,
      DashboardWidgetRepository widgetRepository,
      ChartWidgetService chartWidgetService,
      KpiWidgetService kpiWidgetService) {
    this.dashboardRepository = dashboardRepository;
    this.widgetRepository = widgetRepository;
    this.chartWidgetService = chartWidgetService;
    this.kpiWidgetService = kpiWidgetService;
  }

  // get all dashboards owned by user
  @Transactional
  public List<DashboardDTO> getDashboardsByUserId(UUID userId) {
    return dashboardRepository.findByUserId(userId).stream().map(this::mapToDashboardDTO).toList();
  }

  // create new blanck instance of dashbnoard
  @Transactional
  public DashboardDTO createDashboard(DashboardCreateDTO request) {
    UUID userId = request.userId();
    List<Dashboard> existingDashboards = dashboardRepository.findByUserId(userId);
    for (Dashboard existing : existingDashboards) {
      if (Boolean.TRUE.equals(existing.getCurrent())) {
        Dashboard updatedExisting =
            new Dashboard(
                existing.getId(),
                existing.getUserId(),
                existing.getDisplayName(),
                existing.getTimeFrom(),
                existing.getTimeTo(),
                existing.getPredefinedTime(),
                false);
        dashboardRepository.save(updatedExisting);
      }
    }

    Dashboard newDashboard =
        new Dashboard(
            request.id() != null ? request.id() : UUID.randomUUID(),
            userId,
            request.displayName(),
            null,
            null,
            "last_24h",
            true);

    dashboardRepository.save(newDashboard);
    return mapToDashboardDTO(newDashboard);
  }

  // delete existing dashboard that is owned by specific user
  @Transactional
  public void deleteDashboard(UUID userId, UUID dashboardId) {
    Dashboard dashboard = getDashboardAndVerifyOwnership(userId, dashboardId);
    boolean wasCurrent = Boolean.TRUE.equals(dashboard.getCurrent());
    dashboardRepository.delete(dashboard);

    // chane current if current was deleted
    if (wasCurrent) {
      List<Dashboard> remainingDashboards = dashboardRepository.findByUserId(userId);
      if (!remainingDashboards.isEmpty()) {
        Dashboard next = remainingDashboards.get(0);
        Dashboard promotedDashboard =
            new Dashboard(
                next.getId(),
                next.getUserId(),
                next.getDisplayName(),
                next.getTimeFrom(),
                next.getTimeTo(),
                next.getPredefinedTime(),
                true // Mark as current
                );
        dashboardRepository.save(promotedDashboard);
      }
    }
  }

  // batch update dashboard layout after edit mode was saved in frontend
  @Transactional
  public void updateDashboardLayout(
      UUID userId, UUID dashboardId, List<WidgetLayoutUpdateDTO> layouts) {
    getDashboardAndVerifyOwnership(userId, dashboardId);
    for (WidgetLayoutUpdateDTO layout : layouts) {
      widgetRepository
          .findById(layout.id())
          .ifPresent(
              widget -> {
                Widget updatedWidget =
                    new Widget(
                        widget.getId(),
                        widget.getDashboardId(),
                        widget.getType(),
                        layout.x(),
                        layout.y(),
                        layout.w(),
                        layout.h(),
                        widget.getDisplayName());
                if (widget.getDashboardId().equals(dashboardId)) {
                  widgetRepository.save(updatedWidget);
                }
              });
    }
  }

  // add new widget to specific dashboard
  @Transactional
  public WidgetDTO createWidget(UUID userId, UUID dashboardId, WidgetDTO request) {
    getDashboardAndVerifyOwnership(userId, dashboardId);

    UUID widgetId = (request.id() != null) ? request.id() : UUID.randomUUID();

    // Save in shared widget table
    Widget newWidget =
        new Widget(
            widgetId,
            dashboardId,
            request.widgetType(),
            request.startX(),
            request.startY(),
            request.width(),
            request.height(),
            request.displayName());

    widgetRepository.save(newWidget);

    // widget type specific persistence
    switch (request) {
      case KpiWidgetDTO kpi -> kpiWidgetService.createKpiWidget(widgetId, kpi);
      case ChartWidgetDTO chart -> chartWidgetService.createChartWidget(widgetId, chart);
    }

    return mapToWidgetDTO(newWidget);
  }

  // update specific widget's visual or data configuration
  @Transactional
  public void updateWidgetConfig(UUID userId, UUID widgetId, WidgetConfigUpdateDTO request) {
    Widget widget =
        widgetRepository
            .findById(widgetId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Widget not found"));

    getDashboardAndVerifyOwnership(userId, widget.getDashboardId());

    switch (request) {
      case KpiWidgetConfigUpdateDTO kpi -> kpiWidgetService.updateKpiWidget(kpi);
      case ChartWidgetConfigUpdateDTO chart -> chartWidgetService.updateChartWidget(chart);
    }
  }

  private Dashboard getDashboardAndVerifyOwnership(UUID userId, UUID dashboardId) {
    Dashboard dashboard =
        dashboardRepository
            .findById(dashboardId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dashboard not found"));
    if (!dashboard.getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this dashboard");
    }
    return dashboard;
  }

  @Transactional
  public void deleteWidget(UUID userId, UUID widgetId) {
    Widget widget =
        widgetRepository
            .findById(widgetId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Widget not found"));

    getDashboardAndVerifyOwnership(userId, widget.getDashboardId());

    widgetRepository.delete(widget);
  }

  private DashboardDTO mapToDashboardDTO(Dashboard dashboard) {
    List<Widget> widgets = widgetRepository.findByDashboardId(dashboard.getId());

    List<WidgetDTO> widgetDTOs = widgets.stream().map(this::mapToWidgetDTO).toList();

    return new DashboardDTO(
        dashboard.getId(),
        dashboard.getDisplayName(),
        dashboard.getTimeFrom(),
        dashboard.getTimeTo(),
        dashboard.getPredefinedTime(),
        dashboard.getCurrent(),
        widgetDTOs);
  }

  private WidgetDTO mapToWidgetDTO(Widget widget) {
    return switch (widget.getType()) {
      case CHART -> chartWidgetService.mapToChartWidgetDTO(widget);
      case KPI -> kpiWidgetService.mapToKpiWidgetDTO(widget);
    };
  }
}
