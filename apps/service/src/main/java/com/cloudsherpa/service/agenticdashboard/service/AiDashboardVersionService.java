package com.cloudsherpa.service.agenticdashboard.service;

import com.cloudsherpa.lib.entities.AiChartWidget;
import com.cloudsherpa.lib.entities.AiDashboardVersion;
import com.cloudsherpa.lib.entities.AiDashboardWidget;
import com.cloudsherpa.lib.entities.AiKpiWidget;
import com.cloudsherpa.lib.entities.TypeEnum;
import com.cloudsherpa.lib.repositories.AiChartWidgetRepository;
import com.cloudsherpa.lib.repositories.AiDashboardVersionRepository;
import com.cloudsherpa.lib.repositories.AiDashboardWidgetRepository;
import com.cloudsherpa.lib.repositories.AiKpiWidgetRepository;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiDashboardVersionService {

  private final AiDashboardVersionRepository versionRepository;
  private final AiDashboardWidgetRepository widgetRepository;
  private final AiChartWidgetRepository chartWidgetRepository;
  private final AiKpiWidgetRepository kpiWidgetRepository;
  private final AiSessionService sessionService;

  public AiDashboardVersionService(
      AiDashboardVersionRepository versionRepository,
      AiDashboardWidgetRepository widgetRepository,
      AiChartWidgetRepository chartWidgetRepository,
      AiKpiWidgetRepository kpiWidgetRepository,
      AiSessionService sessionService) {
    this.versionRepository = versionRepository;
    this.widgetRepository = widgetRepository;
    this.chartWidgetRepository = chartWidgetRepository;
    this.kpiWidgetRepository = kpiWidgetRepository;
    this.sessionService = sessionService;
  }

  @Transactional
  public AiDashboardVersion createVersion(UUID userId, UUID sessionId, DashboardPlanDto plan) {

    AiDashboardVersion parentVersion =
        versionRepository.findTopBySessionIdOrderByVersionNumberDesc(sessionId).orElse(null);

    int versionNumber = parentVersion == null ? 1 : parentVersion.getVersionNumber() + 1;

    UUID versionId = UUID.randomUUID();

    AiDashboardVersion version =
        AiDashboardVersion.builder()
            .versionId(versionId)
            .sessionId(sessionId)
            .versionNumber(versionNumber)
            .parentVersionId(parentVersion != null ? parentVersion.getVersionId() : null)
            .title(plan.title())
            .description(plan.description())
            .timeFrom(plan.timeFrom())
            .timeTo(plan.timeTo())
            .predefinedTime(plan.predefinedTime())
            .current(true)
            .createdAt(OffsetDateTime.now())
            .build();

    versionRepository.save(version);

    if (parentVersion != null) {
      unsetCurrentVersion(parentVersion);
    }

    for (DashboardPlanWidgetDto widgetDto : plan.widgets()) {
      saveWidget(versionId, widgetDto);
    }

    sessionService.updateCurrentVersion(userId, sessionId, versionId);

    return version;
  }

  @Transactional(readOnly = true)
  public List<AiDashboardVersion> getVersions(UUID userId, UUID sessionId) {

    sessionService.getSession(userId, sessionId);

    return versionRepository.findBySessionIdOrderByVersionNumberDesc(sessionId);
  }

  @Transactional(readOnly = true)
  public AiDashboardVersion getVersion(UUID userId, UUID sessionId, UUID versionId) {

    sessionService.getSession(userId, sessionId);

    return versionRepository
        .findByVersionIdAndSessionId(versionId, sessionId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "AI dashboard version not found"));
  }

  private void unsetCurrentVersion(AiDashboardVersion version) {
    version.setCurrent(false);
    versionRepository.save(version);
  }

  private void saveWidget(UUID dashboardVersionId, DashboardPlanWidgetDto widgetDto) {

    UUID widgetId = UUID.randomUUID();

    AiDashboardWidget widget =
        AiDashboardWidget.builder()
            .widgetId(widgetId)
            .dashboardVersionId(dashboardVersionId)
            .widgetType(widgetDto.widgetType())
            .startX(widgetDto.startX())
            .startY(widgetDto.startY())
            .width(widgetDto.width())
            .height(widgetDto.height())
            .displayName(widgetDto.displayName())
            .build();

    widgetRepository.save(widget);

    if (widgetDto.widgetType() == TypeEnum.CHART) {
      saveChartWidget(widgetId, widget, widgetDto);
    } else if (widgetDto.widgetType() == TypeEnum.KPI) {
      saveKpiWidget(widgetId, widget, widgetDto);
    }
  }

  private void saveChartWidget(
      UUID widgetId, AiDashboardWidget widget, DashboardPlanWidgetDto widgetDto) {

    AiChartWidget chartWidget =
        AiChartWidget.builder()
            .widgetId(widgetId)
            .widget(widget)
            .chartType(widgetDto.chartType())
            .chartColour(widgetDto.chartColour())
            .provider(widgetDto.provider())
            .accountId(widgetDto.accountId())
            .resourceId(widgetDto.resourceId())
            .metricType(widgetDto.metricType())
            .metricName(widgetDto.metricName())
            .build();

    chartWidgetRepository.save(chartWidget);
  }

  private void saveKpiWidget(
      UUID widgetId, AiDashboardWidget widget, DashboardPlanWidgetDto widgetDto) {

    AiKpiWidget kpiWidget =
        AiKpiWidget.builder()
            .widgetId(widgetId)
            .widget(widget)
            .chargeIds(widgetDto.chargeIds())
            .aggregationWindowDays(widgetDto.aggregationWindowDays())
            .build();

    kpiWidgetRepository.save(kpiWidget);
  }
}
