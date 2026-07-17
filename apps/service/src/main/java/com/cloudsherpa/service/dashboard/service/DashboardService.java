package com.cloudsherpa.service.dashboard.service;

import com.cloudsherpa.lib.entities.Dashboard;
import com.cloudsherpa.lib.entities.Widget;
import com.cloudsherpa.lib.entities.WidgetResource;
import com.cloudsherpa.lib.repositories.DashboardRepository;
import com.cloudsherpa.lib.repositories.DashboardWidgetRepository;
import com.cloudsherpa.lib.repositories.WidgetResourceRepository;
import com.cloudsherpa.service.dashboard.dto.DashboardCreateDTO;
import com.cloudsherpa.service.dashboard.dto.DashboardDTO;
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
  private final WidgetResourceRepository widgetResourceRepository;

  public DashboardService(
      DashboardRepository dashboardRepository,
      DashboardWidgetRepository widgetRepository,
      WidgetResourceRepository widgetResourceRepository) {
    this.dashboardRepository = dashboardRepository;
    this.widgetRepository = widgetRepository;
    this.widgetResourceRepository = widgetResourceRepository;
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
    if (Boolean.TRUE.equals(dashboard.getCurrent())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot delete the current dashboard");
    }
    dashboardRepository.delete(dashboard);
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
                widgetRepository.save(updatedWidget);
              });
    }
  }

  // add new widget to specific dashboard
  @Transactional
  public WidgetDTO createWidget(UUID dashboardId, WidgetDTO request) {
    UUID userId = request.userId();
    getDashboardAndVerifyOwnership(userId, dashboardId);
    Widget widget =
        new Widget(
            request.id() != null ? request.id() : UUID.randomUUID(),
            dashboardId,
            request.type(),
            request.startX(),
            request.startY(),
            request.width(),
            request.height(),
            request.displayName());
    widgetRepository.save(widget);

    if (request.resourceId() != null && request.metricType() != null) {
      WidgetResource resource =
          new WidgetResource(
              UUID.randomUUID(), widget.getId(), request.resourceId(), request.metricType());
      widgetResourceRepository.save(resource);
    }
    return request;
  }

  // update specific widget's visual or data configuration
  @Transactional
  public WidgetDTO updateWidgetConfig(UUID widgetId, WidgetConfigUpdateDTO request) {
    UUID userId = request.userId();
    Widget widget =
        widgetRepository
            .findById(widgetId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Widget not found"));

    getDashboardAndVerifyOwnership(userId, widget.getDashboardId());

    Widget updatedWidget =
        new Widget(
            widget.getId(),
            widget.getDashboardId(),
            request.type(),
            widget.getStartX(),
            widget.getStartY(),
            widget.getWidth(),
            widget.getHeight(),
            request.displayName());

    widgetRepository.save(updatedWidget);

    List<WidgetResource> existingResources = widgetResourceRepository.findByWidgetId(widgetId);
    if (!existingResources.isEmpty()) {
      widgetResourceRepository.deleteAll(existingResources);
    }

    if (request.resourceId() != null && request.metricType() != null) {
      WidgetResource resource =
          new WidgetResource(
              UUID.randomUUID(), widgetId, request.resourceId(), request.metricType());
      widgetResourceRepository.save(resource);
    }

    return new WidgetDTO(
        userId,
        updatedWidget.getId(),
        updatedWidget.getType(),
        updatedWidget.getDisplayName(),
        updatedWidget.getStartX(),
        updatedWidget.getStartY(),
        updatedWidget.getWidth(),
        updatedWidget.getHeight(),
        request.resourceId(),
        request.metricType());
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

  private DashboardDTO mapToDashboardDTO(Dashboard dashboard) {
    List<Widget> widgets = widgetRepository.findByDashboardId(dashboard.getId());

    List<WidgetDTO> widgetDTOs =
        widgets.stream()
            .map(
                widget -> {
                  UUID resourceId = null;
                  String metricType = null;

                  List<WidgetResource> resources =
                      widgetResourceRepository.findByWidgetId(widget.getId());
                  if (!resources.isEmpty()) {
                    resourceId = resources.get(0).getResourceId();
                    metricType = resources.get(0).getMetricType();
                  }

                  return new WidgetDTO(
                      dashboard.getUserId(),
                      widget.getId(),
                      widget.getType(),
                      widget.getDisplayName(),
                      widget.getStartX(),
                      widget.getStartY(),
                      widget.getWidth(),
                      widget.getHeight(),
                      resourceId,
                      metricType);
                })
            .toList();

    return new DashboardDTO(
        dashboard.getId(),
        dashboard.getDisplayName(),
        dashboard.getTimeFrom(),
        dashboard.getTimeTo(),
        dashboard.getPredefinedTime(),
        dashboard.getCurrent(),
        widgetDTOs);
  }

  @Transactional
  public void deleteWidget(UUID userId, UUID widgetId) {
    Widget widget =
        widgetRepository
            .findById(widgetId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Widget not found"));

    getDashboardAndVerifyOwnership(userId, widget.getDashboardId());

    List<WidgetResource> resources = widgetResourceRepository.findByWidgetId(widgetId);
    if (!resources.isEmpty()) {
      widgetResourceRepository.deleteAll(resources);
    }

    widgetRepository.delete(widget);
  }
}
