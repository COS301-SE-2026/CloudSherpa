package com.cloudsherpa.service.agenticdashboard.service;

import com.cloudsherpa.lib.entities.AiDashboardVersion;
import com.cloudsherpa.lib.entities.TypeEnum;
import com.cloudsherpa.service.agenticdashboard.dto.AiDashboardApplyMode;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto;
import com.cloudsherpa.service.dashboard.dto.ChartWidgetDTO;
import com.cloudsherpa.service.dashboard.dto.DashboardDTO;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetDTO;
import com.cloudsherpa.service.dashboard.service.DashboardService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiDashboardApplyService {

  private final AiDashboardVersionService versionService;
  private final DashboardService dashboardService;
  private final AiSessionService sessionService;

  public AiDashboardApplyService(
      AiDashboardVersionService versionService,
      DashboardService dashboardService,
      AiSessionService sessionService) {
    this.versionService = versionService;
    this.dashboardService = dashboardService;
    this.sessionService = sessionService;
  }

  @Transactional
  public List<DashboardDTO> applyVersion(
      UUID userId,
      UUID sessionId,
      UUID versionId,
      AiDashboardApplyMode mode,
      UUID startedDashboardId) {

    if (mode == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apply mode is required");
    }

    AiDashboardVersion version = versionService.getVersion(userId, sessionId, versionId);
    if (version.getVersionNumber() == 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The original dashboard version cannot be applied");
    }

    DashboardPlanDto plan = versionService.getDashboardPlan(userId, sessionId, versionId);

    DashboardDTO dashboard;

    if (mode == AiDashboardApplyMode.REPLACE_STARTED_DASHBOARD) {
      if (startedDashboardId == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "The started dashboard ID is required when replacing a dashboard");
      }

      dashboard = dashboardService.replaceDashboardFromPlan(userId, startedDashboardId, plan);
    } else {
      dashboard = dashboardService.createDashboardFromPlan(userId, UUID.randomUUID(), plan);
    }

    for (DashboardPlanWidgetDto widget : plan.widgets()) {
      createWidget(userId, dashboard.id(), widget);
    }

    List<DashboardDTO> dashboards = dashboardService.getDashboardsByUserId(userId);

    sessionService.deleteSession(userId, sessionId);

    return dashboards;
  }

  private void createWidget(UUID userId, UUID dashboardId, DashboardPlanWidgetDto widget) {

    if (widget.widgetType() == TypeEnum.CHART) {
      dashboardService.createWidget(
          userId,
          dashboardId,
          new ChartWidgetDTO(
              null,
              widget.widgetType(),
              widget.displayName(),
              widget.startX(),
              widget.startY(),
              widget.width(),
              widget.height(),
              widget.chartType(),
              widget.chartColour(),
              widget.provider(),
              widget.accountId(),
              widget.resourceId(),
              widget.metricType(),
              widget.metricName()));

      return;
    }

    dashboardService.createWidget(
        userId,
        dashboardId,
        new KpiWidgetDTO(
            null,
            widget.widgetType(),
            widget.displayName(),
            widget.startX(),
            widget.startY(),
            widget.width(),
            widget.height(),
            widget.chargeIds(),
            widget.aggregationWindowDays()));
  }
}
