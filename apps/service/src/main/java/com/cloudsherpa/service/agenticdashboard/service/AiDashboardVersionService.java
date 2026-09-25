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
import com.cloudsherpa.service.agenticdashboard.dto.AiVersionResponseDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiDashboardVersionService {
  private final EntityManager entityManager;
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
      AiSessionService sessionService,
      EntityManager entityManager) {

    this.versionRepository = versionRepository;
    this.widgetRepository = widgetRepository;
    this.chartWidgetRepository = chartWidgetRepository;
    this.kpiWidgetRepository = kpiWidgetRepository;
    this.sessionService = sessionService;
    this.entityManager = entityManager;
  }

  @Transactional
  public AiDashboardVersion createVersion(UUID userId, UUID sessionId, DashboardPlanDto plan) {

    sessionService.getSession(userId, sessionId);

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

    if (parentVersion != null) {
      unsetCurrentVersion(parentVersion);
    }

    versionRepository.save(version);

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

  /**
   * Returns a complete DTO representation of a staged dashboard version.
   *
   * <p>This is used by the AI response and by the frontend when displaying an individual staged
   * version
   */
  @Transactional(readOnly = true)
  public AiVersionResponseDto getVersionResponse(UUID userId, UUID sessionId, UUID versionId) {

    AiDashboardVersion version = getVersion(userId, sessionId, versionId);

    DashboardPlanDto dashboard = toDashboardPlan(version);

    return new AiVersionResponseDto(
        version.getVersionId(),
        version.getSessionId(),
        version.getVersionNumber(),
        version.getParentVersionId(),
        version.getCurrent(),
        version.getCreatedAt(),
        dashboard);
  }

  /**
   * Returns the dashboard-plan representation of a staged version Used when a user applies a staged
   * version to a real dashboard
   */
  @Transactional(readOnly = true)
  public DashboardPlanDto getDashboardPlan(UUID userId, UUID sessionId, UUID versionId) {

    AiDashboardVersion version = getVersion(userId, sessionId, versionId);

    return toDashboardPlan(version);
  }

  private DashboardPlanDto toDashboardPlan(AiDashboardVersion version) {

    List<DashboardPlanWidgetDto> widgets =
        widgetRepository.findByDashboardVersionId(version.getVersionId()).stream()
            .map(this::mapWidget)
            .toList();

    return new DashboardPlanDto(
        version.getTitle(),
        version.getDescription(),
        version.getTimeFrom(),
        version.getTimeTo(),
        version.getPredefinedTime(),
        widgets);
  }

  private DashboardPlanWidgetDto mapWidget(AiDashboardWidget widget) {

    if (widget.getWidgetType() == TypeEnum.CHART) {

      AiChartWidget chartWidget = chartWidgetRepository.findByWidgetId(widget.getWidgetId());

      if (chartWidget == null) {
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "Chart configuration not found for AI widget");
      }

      return new DashboardPlanWidgetDto(
          widget.getWidgetId(),
          widget.getWidgetType(),
          widget.getDisplayName(),
          widget.getStartX(),
          widget.getStartY(),
          widget.getWidth(),
          widget.getHeight(),
          chartWidget.getChartType(),
          chartWidget.getChartColour(),
          chartWidget.getProvider(),
          null,
          chartWidget.getAccountId(),
          chartWidget.getResourceId(),
          chartWidget.getMetricType(),
          chartWidget.getMetricName(),
          null,
          null);
    }

    if (widget.getWidgetType() == TypeEnum.KPI) {

      AiKpiWidget kpiWidget = kpiWidgetRepository.findByWidgetId(widget.getWidgetId());

      if (kpiWidget == null) {
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "KPI configuration not found for AI widget");
      }

      List<String> chargeIds =
          kpiWidget.getChargeIds() == null ? List.of() : Arrays.asList(kpiWidget.getChargeIds());

      return new DashboardPlanWidgetDto(
          widget.getWidgetId(),
          widget.getWidgetType(),
          widget.getDisplayName(),
          widget.getStartX(),
          widget.getStartY(),
          widget.getWidth(),
          widget.getHeight(),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          chargeIds,
          kpiWidget.getAggregationWindowDays());
    }

    throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR, "Unsupported AI widget type: " + widget.getWidgetType());
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

    AiDashboardWidget managedWidget = widgetRepository.save(widget);

    if (widgetDto.widgetType() == TypeEnum.CHART) {
      saveChartWidget(widgetId, managedWidget, widgetDto);

    } else if (widgetDto.widgetType() == TypeEnum.KPI) {
      saveKpiWidget(widgetId, managedWidget, widgetDto);
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

    entityManager.persist(chartWidget);
  }

  private void saveKpiWidget(
      UUID widgetId, AiDashboardWidget widget, DashboardPlanWidgetDto widgetDto) {

    AiKpiWidget kpiWidget =
        AiKpiWidget.builder()
            .widgetId(widgetId)
            .widget(widget)
            .chargeIds(widgetDto.chargeIds().toArray(String[]::new))
            .aggregationWindowDays(widgetDto.aggregationWindowDays())
            .build();

    entityManager.persist(kpiWidget);
  }
}
