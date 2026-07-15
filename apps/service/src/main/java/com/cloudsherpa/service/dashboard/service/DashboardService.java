package com.cloudsherpa.service.dashboard.service;

import com.cloudsherpa.lib.entities.Dashboard;
import com.cloudsherpa.lib.entities.Widget;
import com.cloudsherpa.lib.entities.WidgetResource;
import com.cloudsherpa.lib.repositories.DashboardRepository;
import com.cloudsherpa.lib.repositories.DashboardWidgetRepository;
import com.cloudsherpa.lib.repositories.WidgetResourceRepository;
import com.cloudsherpa.service.dashboard.dto.DashboardSaveRequestDTO;
import com.cloudsherpa.service.dashboard.dto.WidgetDTO;
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

  @Transactional
  public List<DashboardSaveRequestDTO> getDashboardsByUserId(UUID userId) {
    List<Dashboard> dashboards = dashboardRepository.findByUserId(userId);

    return dashboards.stream().map(this::mapToDashboardDTO).toList();
  }

  @Transactional
  public DashboardSaveRequestDTO saveDashboard(UUID userId, DashboardSaveRequestDTO request) {
    if (request.getId() != null) {
      verifyOwnershipIfExists(userId, request.getId());
    }

    Dashboard dashboard = persistDashboardEntity(userId, request);

    if (request.getWidgets() != null && !request.getWidgets().isEmpty()) {
      syncWidgets(dashboard, request.getWidgets());
    }

    return request;
  }

  @Transactional
  public void deleteDashboard(UUID userId, UUID dashboardId) {
    if (dashboardId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dashboard ID is required");
    }

    Dashboard dashboard =
        dashboardRepository
            .findById(dashboardId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dashboard not found"));

    if (!dashboard.getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this dashboard");
    }

    dashboardRepository.delete(dashboard);
  }

  private void verifyOwnershipIfExists(UUID userId, UUID dashboardId) {
    if (dashboardId == null) return;

    dashboardRepository
        .findById(dashboardId)
        .ifPresent(
            existing -> {
              if (!existing.getUserId().equals(userId)) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied to this dashboard");
              }
            });
  }

  private Dashboard persistDashboardEntity(UUID userId, DashboardSaveRequestDTO payload) {
    UUID dashboardId = payload.getId() != null ? payload.getId() : UUID.randomUUID();

    Dashboard dashboard =
        new Dashboard(
            dashboardId,
            userId,
            payload.getTimeFrom(),
            payload.getTimeTo(),
            payload.getPredefinedTime());

    return dashboardRepository.save(dashboard);
  }

  private void syncWidgets(Dashboard dashboard, List<WidgetDTO> incomingWidgets) {
    List<Widget> existingWidgets = widgetRepository.findByDashboardId(dashboard.getId());
    List<UUID> incomingWidgetIds =
        incomingWidgets.stream()
            .filter(w -> w != null && w.getId() != null)
            .map(WidgetDTO::getId)
            .toList();

    List<Widget> toDelete =
        existingWidgets.stream().filter(w -> !incomingWidgetIds.contains(w.getId())).toList();

    if (!toDelete.isEmpty()) {
      widgetRepository.deleteAll(toDelete);
    }

    for (WidgetDTO dto : incomingWidgets) {
      if (dto != null) {
        Widget widget = persistWidgetEntity(dashboard.getId(), dto);
        persistWidgetResource(widget.getId(), dto);
      }
    }
  }

  private Widget persistWidgetEntity(UUID dashboardId, WidgetDTO dto) {
    UUID widgetId = dto.getId() != null ? dto.getId() : UUID.randomUUID();

    Widget widget =
        new Widget(
            widgetId,
            dashboardId,
            dto.getType(),
            dto.getStartX(),
            dto.getStartY(),
            dto.getWidth(),
            dto.getHeight());

    return widgetRepository.save(widget);
  }

  private void persistWidgetResource(UUID widgetId, WidgetDTO dto) {
    if (dto.getResourceId() == null || dto.getMetricType() == null) {
      return;
    }

    List<WidgetResource> existingResources = widgetResourceRepository.findByWidgetId(widgetId);

    if (existingResources.isEmpty()) {
      WidgetResource resource =
          new WidgetResource(UUID.randomUUID(), widgetId, dto.getResourceId(), dto.getMetricType());
      widgetResourceRepository.save(resource);
    } else {
      widgetResourceRepository.deleteAll(existingResources);

      WidgetResource resource =
          new WidgetResource(UUID.randomUUID(), widgetId, dto.getResourceId(), dto.getMetricType());
      widgetResourceRepository.save(resource);
    }
  }

  private DashboardSaveRequestDTO mapToDashboardDTO(Dashboard dashboard) {
    DashboardSaveRequestDTO dto = new DashboardSaveRequestDTO();
    dto.setId(dashboard.getId());
    dto.setTimeFrom(dashboard.getTimeFrom());
    dto.setTimeTo(dashboard.getTimeTo());
    dto.setPredefinedTime(dashboard.getPredefinedTime());

    List<Widget> widgets = widgetRepository.findByDashboardId(dashboard.getId());

    List<WidgetDTO> widgetDTOs =
        widgets.stream()
            .map(
                widget -> {
                  WidgetDTO wDto = new WidgetDTO();
                  wDto.setId(widget.getId());
                  wDto.setType(widget.getType());
                  wDto.setDisplayName(widget.getDisplayName());
                  wDto.setStartX(widget.getStartX());
                  wDto.setStartY(widget.getStartY());
                  wDto.setWidth(widget.getWidth());
                  wDto.setHeight(widget.getHeight());

                  widgetResourceRepository.findByWidgetId(widget.getId()).stream()
                      .findFirst()
                      .ifPresent(
                          resource -> {
                            wDto.setResourceId(resource.getResourceId());
                            wDto.setMetricType(resource.getMetricType());
                          });
                  return wDto;
                })
            .toList();

    dto.setWidgets(widgetDTOs);
    return dto;
  }
}
