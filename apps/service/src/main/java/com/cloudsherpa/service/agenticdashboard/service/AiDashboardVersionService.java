package com.cloudsherpa.service.agenticdashboard.service;

import com.cloudsherpa.lib.entities.AiChartWidget;
import com.cloudsherpa.lib.entities.AiDashboardVersion;
import com.cloudsherpa.lib.entities.AiDashboardWidget;
import com.cloudsherpa.lib.entities.AiKpiWidget;
import com.cloudsherpa.lib.entities.ChartColourEnum;
import com.cloudsherpa.lib.entities.ChartTypeEnum;
import com.cloudsherpa.lib.entities.PredefinedTimeEnum;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.entities.TypeEnum;
import com.cloudsherpa.lib.repositories.AiChartWidgetRepository;
import com.cloudsherpa.lib.repositories.AiDashboardVersionRepository;
import com.cloudsherpa.lib.repositories.AiDashboardWidgetRepository;
import com.cloudsherpa.lib.repositories.AiKpiWidgetRepository;
import com.cloudsherpa.lib.repositories.OfferedMetricRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.agenticdashboard.dto.AiVersionResponseDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.AddChartWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.AddKpiWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateChartWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateDashboardToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateKpiWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateWidgetLayoutToolDto;
import com.cloudsherpa.service.agenticdashboard.validation.DashboardPlanValidator;
import com.cloudsherpa.service.billing.service.BillingService;
import com.cloudsherpa.service.dashboard.dto.ChartWidgetDTO;
import com.cloudsherpa.service.dashboard.dto.DashboardDTO;
import com.cloudsherpa.service.dashboard.dto.KpiWidgetDTO;
import com.cloudsherpa.service.dashboard.dto.WidgetDTO;
import com.cloudsherpa.service.dashboard.service.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiDashboardVersionService {
  private static final int INITIAL_VERSION_NUMBER = 0;
  private static final int DEFAULT_WIDGET_WIDTH = 4;
  private static final int DEFAULT_WIDGET_HEIGHT = 3;
  private static final int MAX_GRID_WIDTH = 12;

  private final EntityManager entityManager;
  private final AiDashboardVersionRepository versionRepository;
  private final AiDashboardWidgetRepository widgetRepository;
  private final AiChartWidgetRepository chartWidgetRepository;
  private final AiKpiWidgetRepository kpiWidgetRepository;
  private final ResourceRepository resourceRepository;
  private final OfferedMetricRepository offeredMetricRepository;
  private final AiSessionService sessionService;
  private final AiDashboardDraftService draftService;
  private final DashboardService dashboardService;
  private final DashboardPlanValidator dashboardPlanValidator;
  private final ObjectMapper objectMapper;
  private final BillingService billingService;

  public AiDashboardVersionService(
      AiDashboardVersionRepository versionRepository,
      AiDashboardWidgetRepository widgetRepository,
      AiChartWidgetRepository chartWidgetRepository,
      AiKpiWidgetRepository kpiWidgetRepository,
      ResourceRepository resourceRepository,
      OfferedMetricRepository offeredMetricRepository,
      AiSessionService sessionService,
      AiDashboardDraftService draftService,
      DashboardService dashboardService,
      DashboardPlanValidator dashboardPlanValidator,
      ObjectMapper objectMapper,
      BillingService billingService,
      EntityManager entityManager) {

    this.versionRepository = versionRepository;
    this.widgetRepository = widgetRepository;
    this.chartWidgetRepository = chartWidgetRepository;
    this.kpiWidgetRepository = kpiWidgetRepository;
    this.resourceRepository = resourceRepository;
    this.offeredMetricRepository = offeredMetricRepository;
    this.sessionService = sessionService;
    this.draftService = draftService;
    this.dashboardService = dashboardService;
    this.dashboardPlanValidator = dashboardPlanValidator;
    this.objectMapper = objectMapper;
    this.billingService = billingService;
    this.entityManager = entityManager;
  }

  @Transactional
  public AiDashboardVersion createInitialVersion(
      UUID userId, UUID sessionId, UUID sourceDashboardId) {

    var session = sessionService.getSession(userId, sessionId);

    if (session.getCurrentVersionId() != null) {
      return findVersion(sessionId, session.getCurrentVersionId());
    }

    DashboardDTO sourceDashboard = dashboardService.getDashboard(userId, sourceDashboardId);

    DashboardPlanDto baselinePlan = toDashboardPlan(sourceDashboard);
    UUID versionId = UUID.randomUUID();

    AiDashboardVersion version =
        AiDashboardVersion.builder()
            .versionId(versionId)
            .sessionId(sessionId)
            .versionNumber(INITIAL_VERSION_NUMBER)
            .parentVersionId(null)
            .title(sourceDashboard.displayName())
            .description(null)
            .timeFrom(baselinePlan.timeFrom())
            .timeTo(baselinePlan.timeTo())
            .predefinedTime(baselinePlan.predefinedTime())
            .current(true)
            .createdAt(OffsetDateTime.now())
            .build();

    versionRepository.save(version);

    for (DashboardPlanWidgetDto widgetDto : baselinePlan.widgets()) {
      saveWidget(versionId, widgetDto);
    }

    sessionService.updateCurrentVersion(userId, sessionId, versionId);
    return version;
  }

  @Transactional(readOnly = true)
  public boolean hasWorkingDraft(UUID userId, UUID sessionId) {
    sessionService.getSession(userId, sessionId);
    return draftService.findDraft(sessionId).isPresent();
  }

  @Transactional(readOnly = true)
  public DashboardPlanDto getWorkingDashboard(UUID userId, UUID sessionId) {
    sessionService.getSession(userId, sessionId);

    return draftService
        .findDraft(sessionId)
        .orElseGet(
            () -> {
              var session = sessionService.getSession(userId, sessionId);
              if (session.getCurrentVersionId() == null) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "AI session has no starting dashboard");
              }
              return getDashboardPlan(userId, sessionId, session.getCurrentVersionId());
            });
  }

  @Transactional
  public DashboardPlanWidgetDto addChartWidget(
      UUID userId, UUID sessionId, AddChartWidgetToolDto request) {

    DashboardPlanDto draft = ensureDraft(userId, sessionId);
    validateText(request.displayName(), "displayName", 80);
    requireUuid(request.resourceId(), "resourceId");
    validateText(request.metricName(), "metricName", 100);

    Resource resource =
        resourceRepository
            .findById(request.resourceId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

    UUID accountId = resource.getAccountId();
    var provider = resource.getAccount().getConnection().getProvider();
    validateResourceOwnership(userId, resource);
    validateChartMetric(provider, resource.getResourceType(), request.metricName());

    ChartTypeEnum chartType =
        request.chartType() == null
            ? parseEnum("line_chart", ChartTypeEnum.class, "chartType")
            : parseEnum(request.chartType(), ChartTypeEnum.class, "chartType");
    ChartColourEnum chartColour =
        request.chartColour() == null
            ? parseEnum("chart_1", ChartColourEnum.class, "chartColour")
            : parseEnum(request.chartColour(), ChartColourEnum.class, "chartColour");

    Layout layout =
        resolveLayout(
            draft.widgets(),
            request.startX(),
            request.startY(),
            request.width(),
            request.height(),
            null);

    DashboardPlanWidgetDto widget =
        new DashboardPlanWidgetDto(
            UUID.randomUUID(),
            TypeEnum.CHART,
            request.displayName(),
            layout.startX(),
            layout.startY(),
            layout.width(),
            layout.height(),
            chartType,
            chartColour,
            provider,
            null,
            accountId,
            request.resourceId(),
            resource.getResourceType(),
            request.metricName(),
            null,
            null);

    List<DashboardPlanWidgetDto> widgets = appendWidget(draft.widgets(), widget);
    DashboardPlanDto updated = replaceWidgets(draft, widgets);
    dashboardPlanValidator.validate(updated);
    draftService.saveDraft(sessionId, updated);
    sessionService.updateLastActivity(userId, sessionId);
    return widget;
  }

  @Transactional
  public DashboardPlanWidgetDto addKpiWidget(
      UUID userId, UUID sessionId, AddKpiWidgetToolDto request) {

    DashboardPlanDto draft = ensureDraft(userId, sessionId);
    validateText(request.displayName(), "displayName", 80);
    validateChargeIds(request.chargeIds());

    int aggregationWindowDays =
        request.aggregationWindowDays() == null ? 30 : request.aggregationWindowDays();
    if (aggregationWindowDays <= 0) {
      throw invalid("aggregationWindowDays must be greater than zero");
    }

    Layout layout =
        resolveLayout(
            draft.widgets(),
            request.startX(),
            request.startY(),
            request.width(),
            request.height(),
            null);

    DashboardPlanWidgetDto widget =
        new DashboardPlanWidgetDto(
            UUID.randomUUID(),
            TypeEnum.KPI,
            request.displayName(),
            layout.startX(),
            layout.startY(),
            layout.width(),
            layout.height(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            List.copyOf(request.chargeIds()),
            aggregationWindowDays);

    List<DashboardPlanWidgetDto> widgets = appendWidget(draft.widgets(), widget);
    DashboardPlanDto updated = replaceWidgets(draft, widgets);
    dashboardPlanValidator.validate(updated);
    draftService.saveDraft(sessionId, updated);
    sessionService.updateLastActivity(userId, sessionId);
    return widget;
  }

  @Transactional
  public DashboardPlanWidgetDto updateWidgetLayout(
      UUID userId, UUID sessionId, UpdateWidgetLayoutToolDto request) {

    DashboardPlanDto draft = ensureDraft(userId, sessionId);
    DashboardPlanWidgetDto existing = findWidget(draft, request.widgetId());

    if (request.startX() == null
        && request.startY() == null
        && request.width() == null
        && request.height() == null) {
      throw invalid("At least one layout field must be supplied");
    }

    int width = request.width() == null ? existing.width() : request.width();
    int height = request.height() == null ? existing.height() : request.height();
    validateLayoutDimensions(width, height);

    Layout layout =
        resolveLayout(
            draft.widgets(),
            request.startX() == null ? existing.startX() : request.startX(),
            request.startY() == null ? existing.startY() : request.startY(),
            width,
            height,
            existing.widgetId());

    DashboardPlanWidgetDto updatedWidget =
        copyWidget(
            existing,
            existing.displayName(),
            layout.startX(),
            layout.startY(),
            layout.width(),
            layout.height(),
            existing.chartType(),
            existing.chartColour(),
            existing.provider(),
            existing.accountId(),
            existing.resourceId(),
            existing.metricType(),
            existing.metricName(),
            existing.chargeIds(),
            existing.aggregationWindowDays());

    DashboardPlanDto updated = replaceWidget(draft, updatedWidget);
    dashboardPlanValidator.validate(updated);
    draftService.saveDraft(sessionId, updated);
    sessionService.updateLastActivity(userId, sessionId);
    return updatedWidget;
  }

  @Transactional
  public DashboardPlanWidgetDto updateChartWidget(
      UUID userId, UUID sessionId, UpdateChartWidgetToolDto request) {

    DashboardPlanDto draft = ensureDraft(userId, sessionId);
    DashboardPlanWidgetDto existing = findWidget(draft, request.widgetId());

    if (existing.widgetType() != TypeEnum.CHART) {
      throw invalid("Selected widget is not a chart widget");
    }

    String displayName =
        request.displayName() == null ? existing.displayName() : request.displayName();
    validateText(displayName, "displayName", 80);

    ChartTypeEnum chartType =
        request.chartType() == null
            ? existing.chartType()
            : parseEnum(request.chartType(), ChartTypeEnum.class, "chartType");
    ChartColourEnum chartColour =
        request.chartColour() == null
            ? existing.chartColour()
            : parseEnum(request.chartColour(), ChartColourEnum.class, "chartColour");

    UUID resourceId = request.resourceId() == null ? existing.resourceId() : request.resourceId();
    String metricName = request.metricName() == null ? existing.metricName() : request.metricName();

    if (resourceId == null || metricName == null || metricName.isBlank()) {
      throw invalid("Chart resourceId and metricName are required");
    }

    Resource resource =
        resourceRepository
            .findById(resourceId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
    validateResourceOwnership(userId, resource);

    var provider = resource.getAccount().getConnection().getProvider();
    validateChartMetric(provider, resource.getResourceType(), metricName);

    DashboardPlanWidgetDto updatedWidget =
        copyWidget(
            existing,
            displayName,
            existing.startX(),
            existing.startY(),
            existing.width(),
            existing.height(),
            chartType,
            chartColour,
            provider,
            resource.getAccountId(),
            resourceId,
            resource.getResourceType(),
            metricName,
            null,
            null);

    DashboardPlanDto updated = replaceWidget(draft, updatedWidget);
    dashboardPlanValidator.validate(updated);
    draftService.saveDraft(sessionId, updated);
    sessionService.updateLastActivity(userId, sessionId);
    return updatedWidget;
  }

  @Transactional
  public DashboardPlanWidgetDto updateKpiWidget(
      UUID userId, UUID sessionId, UpdateKpiWidgetToolDto request) {

    DashboardPlanDto draft = ensureDraft(userId, sessionId);
    DashboardPlanWidgetDto existing = findWidget(draft, request.widgetId());

    if (existing.widgetType() != TypeEnum.KPI) {
      throw invalid("Selected widget is not a KPI widget");
    }

    String displayName =
        request.displayName() == null ? existing.displayName() : request.displayName();
    validateText(displayName, "displayName", 80);

    List<String> chargeIds =
        request.chargeIds() == null ? existing.chargeIds() : request.chargeIds();
    Integer aggregationWindowDays =
        request.aggregationWindowDays() == null
            ? existing.aggregationWindowDays()
            : request.aggregationWindowDays();

    validateChargeIds(chargeIds);
    if (aggregationWindowDays == null || aggregationWindowDays <= 0) {
      throw invalid("aggregationWindowDays must be greater than zero");
    }

    DashboardPlanWidgetDto updatedWidget =
        copyWidget(
            existing,
            displayName,
            existing.startX(),
            existing.startY(),
            existing.width(),
            existing.height(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            List.copyOf(chargeIds),
            aggregationWindowDays);

    DashboardPlanDto updated = replaceWidget(draft, updatedWidget);
    dashboardPlanValidator.validate(updated);
    draftService.saveDraft(sessionId, updated);
    sessionService.updateLastActivity(userId, sessionId);
    return updatedWidget;
  }

  @Transactional
  public DashboardPlanWidgetDto deleteWidget(UUID userId, UUID sessionId, UUID widgetId) {
    DashboardPlanDto draft = ensureDraft(userId, sessionId);
    DashboardPlanWidgetDto widget = findWidget(draft, widgetId);

    List<DashboardPlanWidgetDto> widgets =
        draft.widgets().stream().filter(item -> !item.widgetId().equals(widgetId)).toList();

    DashboardPlanDto updated = replaceWidgets(draft, widgets);
    dashboardPlanValidator.validate(updated);
    draftService.saveDraft(sessionId, updated);
    sessionService.updateLastActivity(userId, sessionId);
    return widget;
  }

  @Transactional(readOnly = true)
  public boolean hasUncommittedChanges(UUID userId, UUID sessionId) {
    sessionService.getSession(userId, sessionId);

    Optional<DashboardPlanDto> draft = draftService.findDraft(sessionId);
    if (draft.isEmpty()) {
      return false;
    }

    var session = sessionService.getSession(userId, sessionId);
    if (session.getCurrentVersionId() == null) {
      return false;
    }

    DashboardPlanDto committed = getDashboardPlan(userId, sessionId, session.getCurrentVersionId());

    return !draft.get().equals(committed);
  }

  @Transactional
  public DashboardPlanDto clearWorkingDashboard(UUID userId, UUID sessionId) {
    DashboardPlanDto draft = ensureDraft(userId, sessionId);
    DashboardPlanDto updated = replaceWidgets(draft, List.of());
    dashboardPlanValidator.validate(updated);
    draftService.saveDraft(sessionId, updated);
    sessionService.updateLastActivity(userId, sessionId);
    return updated;
  }

  @Transactional
  public void discardWorkingDashboard(UUID userId, UUID sessionId) {
    sessionService.getSession(userId, sessionId);
    draftService.clearDraft(sessionId);
    sessionService.updateLastActivity(userId, sessionId);
  }

  @Transactional
  public DashboardPlanDto updateDashboard(
      UUID userId, UUID sessionId, UpdateDashboardToolDto request) {

    DashboardPlanDto draft = ensureDraft(userId, sessionId);

    String title = request.title() == null ? draft.title() : request.title();
    String description =
        request.description() == null ? draft.description() : request.description();
    var timeFrom = request.timeFrom() == null ? draft.timeFrom() : request.timeFrom();
    var timeTo = request.timeTo() == null ? draft.timeTo() : request.timeTo();
    PredefinedTimeEnum predefinedTime =
        request.predefinedTime() == null
            ? draft.predefinedTime()
            : parseEnum(request.predefinedTime(), PredefinedTimeEnum.class, "predefinedTime");

    DashboardPlanDto updated =
        new DashboardPlanDto(title, description, timeFrom, timeTo, predefinedTime, draft.widgets());
    dashboardPlanValidator.validate(updated);
    draftService.saveDraft(sessionId, updated);
    sessionService.updateLastActivity(userId, sessionId);
    return updated;
  }

  @Transactional
  public AiVersionResponseDto commitWorkingDashboard(UUID userId, UUID sessionId) {
    DashboardPlanDto draft =
        draftService
            .findDraft(sessionId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.CONFLICT, "There are no uncommitted dashboard changes"));

    dashboardPlanValidator.validate(draft);
    createVersion(userId, sessionId, draft);
    draftService.clearDraft(sessionId);

    var session = sessionService.getSession(userId, sessionId);
    return getVersionResponse(userId, sessionId, session.getCurrentVersionId());
  }

  @Transactional
  public AiDashboardVersion createVersion(UUID userId, UUID sessionId, DashboardPlanDto plan) {

    var session = sessionService.getSession(userId, sessionId);

    AiDashboardVersion parentVersion =
        session.getCurrentVersionId() == null
            ? null
            : versionRepository
                .findByVersionIdAndSessionId(session.getCurrentVersionId(), sessionId)
                .orElseThrow(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Active AI dashboard version not found"));

    int versionNumber =
        versionRepository
            .findTopBySessionIdOrderByVersionNumberDesc(sessionId)
            .map(version -> version.getVersionNumber() + 1)
            .orElse(1);

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

    for (AiDashboardVersion existingVersion :
        versionRepository.findBySessionIdOrderByVersionNumberDesc(sessionId)) {
      if (Boolean.TRUE.equals(existingVersion.getCurrent())) {
        existingVersion.setCurrent(false);
        versionRepository.save(existingVersion);
      }
    }

    versionRepository.save(version);

    for (DashboardPlanWidgetDto widgetDto : plan.widgets()) {
      saveWidget(versionId, widgetDto);
    }

    sessionService.updateCurrentVersion(userId, sessionId, versionId);
    return version;
  }

  @Transactional
  public AiVersionResponseDto activateVersion(UUID userId, UUID sessionId, UUID versionId) {

    sessionService.getSession(userId, sessionId);

    if (draftService.findDraft(sessionId).isPresent()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Commit or discard the working dashboard before activating a version");
    }

    AiDashboardVersion target = findVersion(sessionId, versionId);

    for (AiDashboardVersion version :
        versionRepository.findBySessionIdOrderByVersionNumberDesc(sessionId)) {
      boolean shouldBeCurrent = version.getVersionId().equals(target.getVersionId());
      if (Boolean.TRUE.equals(version.getCurrent()) != shouldBeCurrent) {
        version.setCurrent(shouldBeCurrent);
        versionRepository.save(version);
      }
    }

    sessionService.updateCurrentVersion(userId, sessionId, versionId);
    return buildVersionResponse(target);
  }

  @Transactional(readOnly = true)
  public List<AiDashboardVersion> getVersions(UUID userId, UUID sessionId) {
    sessionService.getSession(userId, sessionId);
    return versionRepository.findBySessionIdOrderByVersionNumberDesc(sessionId);
  }

  @Transactional(readOnly = true)
  public AiDashboardVersion getVersion(UUID userId, UUID sessionId, UUID versionId) {
    sessionService.getSession(userId, sessionId);
    return findVersion(sessionId, versionId);
  }

  @Transactional(readOnly = true)
  public AiVersionResponseDto getVersionResponse(UUID userId, UUID sessionId, UUID versionId) {
    sessionService.getSession(userId, sessionId);
    AiDashboardVersion version = findVersion(sessionId, versionId);
    return buildVersionResponse(version);
  }

  @Transactional(readOnly = true)
  public DashboardPlanDto getDashboardPlan(UUID userId, UUID sessionId, UUID versionId) {
    sessionService.getSession(userId, sessionId);
    AiDashboardVersion version = findVersion(sessionId, versionId);
    return toDashboardPlan(version);
  }

  private DashboardPlanDto ensureDraft(UUID userId, UUID sessionId) {
    var session = sessionService.getSession(userId, sessionId);

    return draftService
        .findDraft(sessionId)
        .orElseGet(
            () -> {
              if (session.getCurrentVersionId() == null) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "AI session has no starting dashboard");
              }
              DashboardPlanDto baseline =
                  getDashboardPlan(userId, sessionId, session.getCurrentVersionId());
              dashboardPlanValidator.validate(baseline);
              draftService.saveDraft(sessionId, baseline);
              return baseline;
            });
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

  private DashboardPlanDto toDashboardPlan(DashboardDTO dashboard) {
    List<DashboardPlanWidgetDto> widgets =
        dashboard.widgets().stream().map(this::mapSourceWidget).toList();

    return new DashboardPlanDto(
        dashboard.displayName(),
        null,
        dashboard.timeFrom(),
        dashboard.timeTo(),
        dashboard.predefinedTime(),
        widgets);
  }

  private DashboardPlanWidgetDto mapSourceWidget(WidgetDTO widget) {
    return switch (widget) {
      case ChartWidgetDTO(
              UUID id,
              TypeEnum widgetType,
              String displayName,
              Integer startX,
              Integer startY,
              Integer width,
              Integer height,
              var chartType,
              var chartColour,
              var provider,
              UUID accountId,
              UUID resourceId,
              String metricType,
              String metricName) ->
          new DashboardPlanWidgetDto(
              id,
              widgetType,
              displayName,
              startX,
              startY,
              width,
              height,
              chartType,
              chartColour,
              provider,
              null,
              accountId,
              resourceId,
              metricType,
              metricName,
              null,
              null);
      case KpiWidgetDTO(
              UUID id,
              TypeEnum widgetType,
              String displayName,
              Integer startX,
              Integer startY,
              Integer width,
              Integer height,
              List<String> chargeIds,
              Integer aggregationWindowDays) ->
          new DashboardPlanWidgetDto(
              id,
              widgetType,
              displayName,
              startX,
              startY,
              width,
              height,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              chargeIds,
              aggregationWindowDays);
    };
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

  private AiDashboardVersion findVersion(UUID sessionId, UUID versionId) {
    return versionRepository
        .findByVersionIdAndSessionId(versionId, sessionId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "AI dashboard version not found"));
  }

  private AiVersionResponseDto buildVersionResponse(AiDashboardVersion version) {
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

  private DashboardPlanDto replaceWidget(
      DashboardPlanDto draft, DashboardPlanWidgetDto updatedWidget) {

    List<DashboardPlanWidgetDto> widgets =
        draft.widgets().stream()
            .map(
                widget ->
                    widget.widgetId().equals(updatedWidget.widgetId()) ? updatedWidget : widget)
            .toList();

    return replaceWidgets(draft, widgets);
  }

  private DashboardPlanDto replaceWidgets(
      DashboardPlanDto draft, List<DashboardPlanWidgetDto> widgets) {
    return new DashboardPlanDto(
        draft.title(),
        draft.description(),
        draft.timeFrom(),
        draft.timeTo(),
        draft.predefinedTime(),
        List.copyOf(widgets));
  }

  private List<DashboardPlanWidgetDto> appendWidget(
      List<DashboardPlanWidgetDto> widgets, DashboardPlanWidgetDto widget) {
    List<DashboardPlanWidgetDto> result = new ArrayList<>(widgets);
    result.add(widget);
    return result;
  }

  private DashboardPlanWidgetDto findWidget(DashboardPlanDto draft, UUID widgetId) {
    if (widgetId == null) {
      throw invalid("widgetId is required");
    }

    return draft.widgets().stream()
        .filter(widget -> widgetId.equals(widget.widgetId()))
        .findFirst()
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Widget not found in working dashboard"));
  }

  private DashboardPlanWidgetDto copyWidget(
      DashboardPlanWidgetDto source,
      String displayName,
      Integer startX,
      Integer startY,
      Integer width,
      Integer height,
      ChartTypeEnum chartType,
      ChartColourEnum chartColour,
      ProviderEnum provider,
      UUID accountId,
      UUID resourceId,
      String metricType,
      String metricName,
      List<String> chargeIds,
      Integer aggregationWindowDays) {

    return new DashboardPlanWidgetDto(
        source.widgetId(),
        source.widgetType(),
        displayName,
        startX,
        startY,
        width,
        height,
        chartType,
        chartColour,
        source.widgetType() == TypeEnum.CHART ? provider : null,
        null,
        accountId,
        resourceId,
        metricType,
        metricName,
        chargeIds,
        aggregationWindowDays);
  }

  private Layout resolveLayout(
      List<DashboardPlanWidgetDto> widgets,
      Integer requestedStartX,
      Integer requestedStartY,
      Integer requestedWidth,
      Integer requestedHeight,
      UUID ignoredWidgetId) {

    int width = requestedWidth == null ? DEFAULT_WIDGET_WIDTH : requestedWidth;
    int height = requestedHeight == null ? DEFAULT_WIDGET_HEIGHT : requestedHeight;
    validateLayoutDimensions(width, height);

    int startX = requestedStartX == null ? 0 : requestedStartX;
    int startY = requestedStartY == null ? 0 : requestedStartY;
    if (startX < 0 || startY < 0) {
      throw invalid("Widget position must be zero or greater");
    }

    if (fitsWithoutCollision(widgets, startX, startY, width, height, ignoredWidgetId)) {
      return new Layout(startX, startY, width, height);
    }

    if (requestedStartX != null || requestedStartY != null) {
      return findNextFreeLayout(widgets, width, height, ignoredWidgetId);
    }

    return findNextFreeLayout(widgets, width, height, ignoredWidgetId);
  }

  private Layout findNextFreeLayout(
      List<DashboardPlanWidgetDto> widgets, int width, int height, UUID ignoredWidgetId) {

    for (int y = 0; y <= 1000; y++) {
      for (int x = 0; x <= MAX_GRID_WIDTH - width; x++) {
        if (fitsWithoutCollision(widgets, x, y, width, height, ignoredWidgetId)) {
          return new Layout(x, y, width, height);
        }
      }
    }

    throw invalid("No free dashboard space is available for this widget");
  }

  private boolean fitsWithoutCollision(
      List<DashboardPlanWidgetDto> widgets,
      int startX,
      int startY,
      int width,
      int height,
      UUID ignoredWidgetId) {

    if (startX < 0 || startX + width > MAX_GRID_WIDTH || startY < 0) {
      return false;
    }

    for (DashboardPlanWidgetDto widget : widgets) {
      if (ignoredWidgetId != null && ignoredWidgetId.equals(widget.widgetId())) {
        continue;
      }
      if (overlaps(startX, startY, width, height, widget)) {
        return false;
      }
    }

    return true;
  }

  private boolean overlaps(
      int startX, int startY, int width, int height, DashboardPlanWidgetDto widget) {

    int left = widget.startX();
    int right = widget.startX() + widget.width();
    int top = widget.startY();
    int bottom = widget.startY() + widget.height();

    return startX < right && startX + width > left && startY < bottom && startY + height > top;
  }

  private void validateResourceOwnership(UUID userId, Resource resource) {
    if (!resource.getAccount().getConnection().getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }
  }

  private void validateChartMetric(
      com.cloudsherpa.lib.entities.ProviderEnum provider, String resourceType, String metricName) {

    boolean metricExists =
        offeredMetricRepository.findByProviderAndServiceType(provider, resourceType).stream()
            .anyMatch(metric -> metric.getMetricName().equals(metricName));

    if (!metricExists) {
      throw invalid("Metric is not available for the selected resource");
    }
  }

  private void validateChargeIds(List<String> chargeIds) {
    if (chargeIds == null || chargeIds.isEmpty()) {
      throw invalid("At least one billing charge ID is required");
    }

    if (chargeIds.size() > 20) {
      throw invalid("A KPI cannot contain more than 20 billing charge IDs");
    }

    Set<String> unique = new HashSet<>(chargeIds);
    if (unique.size() != chargeIds.size()) {
      throw invalid("Billing charge IDs must be unique");
    }

    Set<String> validChargeIds =
        billingService.getCharges().stream()
            .map(charge -> charge.chargeId())
            .collect(java.util.stream.Collectors.toSet());
    if (!validChargeIds.containsAll(chargeIds)) {
      throw invalid("One or more billing charge IDs are not available");
    }
  }

  private void validateText(String value, String field, int maxLength) {
    if (value == null || value.isBlank()) {
      throw invalid(field + " is required");
    }
    if (value.length() > maxLength) {
      throw invalid(field + " must not exceed " + maxLength + " characters");
    }
  }

  private void validateLayoutDimensions(int width, int height) {
    if (width <= 0 || width > MAX_GRID_WIDTH) {
      throw invalid("Widget width must be between 1 and 12");
    }
    if (height <= 0) {
      throw invalid("Widget height must be greater than zero");
    }
  }

  private void requireUuid(UUID value, String field) {
    if (value == null) {
      throw invalid(field + " is required");
    }
  }

  private <T extends Enum<T>> T parseEnum(String value, Class<T> type, String field) {
    try {
      return objectMapper.convertValue(value, type);
    } catch (IllegalArgumentException exception) {
      throw invalid("Invalid " + field + " value: " + value);
    }
  }

  private ResponseStatusException invalid(String message) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }

  private record Layout(int startX, int startY, int width, int height) {}
}
