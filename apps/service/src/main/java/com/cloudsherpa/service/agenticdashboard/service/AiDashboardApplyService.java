package com.cloudsherpa.service.agenticdashboard.service;

import com.cloudsherpa.lib.entities.TypeEnum;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto;
import com.cloudsherpa.service.dashboard.dto.ChartWidgetDTO;
import com.cloudsherpa.service.dashboard.dto.DashboardCreateDTO;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetDTO;
import com.cloudsherpa.service.dashboard.service.DashboardService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiDashboardApplyService {

  private final AiDashboardVersionService versionService;
  private final DashboardService dashboardService;

  public AiDashboardApplyService(
      AiDashboardVersionService versionService, DashboardService dashboardService) {
    this.versionService = versionService;
    this.dashboardService = dashboardService;
  }

  @Transactional
  public UUID applyVersion(UUID userId, UUID sessionId, UUID versionId) {

    DashboardPlanDto plan = versionService.getDashboardPlan(userId, sessionId, versionId);

    UUID dashboardId = UUID.randomUUID();

    dashboardService.createDashboard(new DashboardCreateDTO(userId, dashboardId, plan.title()));

    for (DashboardPlanWidgetDto widget : plan.widgets()) {
      createWidget(userId, dashboardId, widget);
    }

    return dashboardId;
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
