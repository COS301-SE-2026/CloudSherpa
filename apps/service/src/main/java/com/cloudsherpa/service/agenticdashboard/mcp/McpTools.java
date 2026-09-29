package com.cloudsherpa.service.agenticdashboard.mcp;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.OfferedMetric;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.OfferedMetricRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.agenticdashboard.agent.AiAgentContext;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.AddChartWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.AddKpiWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.BillingChargeToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.CloudAccountToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.MetricToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.ResourceToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateChartWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateDashboardToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateKpiWidgetToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.UpdateWidgetLayoutToolDto;
import com.cloudsherpa.service.agenticdashboard.service.AiDashboardVersionService;
import com.cloudsherpa.service.agenticdashboard.service.AiSessionService;
import com.cloudsherpa.service.billing.dto.BillingChargeResponse;
import com.cloudsherpa.service.billing.service.BillingService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class McpTools {

  private static final int MAX_ACCOUNTS = 10;
  private static final int MAX_RESOURCES = 12;
  private static final int MAX_METRICS = 16;
  private static final int MAX_CHARGES = 16;
  private static final int MAX_WIDGETS = 20;

  private static final String RESULTS = "results";
  private static final String COUNT = "count";
  private static final String LIMIT = "limit";
  private static final String TRUNCATED = "truncated";
  private static final String WIDGET_ID = "widgetId";
  private static final String WIDGET_TYPE = "widgetType";
  private static final String DISPLAY_NAME = "displayName";
  private static final String START_X = "startX";
  private static final String START_Y = "startY";
  private static final String WIDTH = "width";
  private static final String HEIGHT = "height";
  private static final String MESSAGE = "message";

  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;
  private final ResourceRepository resourceRepository;
  private final OfferedMetricRepository offeredMetricRepository;
  private final BillingService billingService;
  private final AiSessionService aiSessionService;
  private final AiDashboardVersionService versionService;

  public McpTools(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      ResourceRepository resourceRepository,
      OfferedMetricRepository offeredMetricRepository,
      BillingService billingService,
      AiSessionService aiSessionService,
      AiDashboardVersionService versionService) {

    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.resourceRepository = resourceRepository;
    this.offeredMetricRepository = offeredMetricRepository;
    this.billingService = billingService;
    this.aiSessionService = aiSessionService;
    this.versionService = versionService;
  }

  public Map<String, Object> findCloudAccounts(
      AiAgentContext context, String query, Integer limit) {
    validateSession(context);
    int resultLimit = normalizeLimit(limit, MAX_ACCOUNTS);
    String normalizedQuery = normalizeQuery(query);

    List<CloudAccountToolDto> results =
        cloudConnectionRepository.findByUserId(context.userId()).stream()
            .flatMap(connection -> mapAccounts(connection).stream())
            .filter(account -> matchesAccount(account, normalizedQuery))
            .limit(resultLimit)
            .toList();

    return Map.of(
        RESULTS,
        results,
        COUNT,
        results.size(),
        LIMIT,
        resultLimit,
        TRUNCATED,
        results.size() >= resultLimit);
  }

  public Map<String, Object> findResources(
      AiAgentContext context, UUID accountId, String query, String resourceType, Integer limit) {

    validateSession(context);
    CloudAccount account = getOwnedAccount(context.userId(), accountId);
    int resultLimit = normalizeLimit(limit, MAX_RESOURCES);
    String normalizedQuery = normalizeQuery(query);
    String normalizedResourceType = normalizeQuery(resourceType);

    List<ResourceToolDto> results =
        resourceRepository.findByAccountId(account.getId()).stream()
            .filter(resource -> matchesResourceType(resource, normalizedResourceType))
            .filter(resource -> matchesResource(resource, normalizedQuery))
            .sorted(
                Comparator.comparing(
                    Resource::getResourceName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
            .limit(resultLimit)
            .map(resource -> mapResource(resource, account.getConnection().getProvider()))
            .toList();

    return Map.of(
        RESULTS,
        results,
        COUNT,
        results.size(),
        LIMIT,
        resultLimit,
        TRUNCATED,
        results.size() >= resultLimit);
  }

  public Map<String, Object> findAvailableMetrics(
      AiAgentContext context, UUID resourceId, String query, Integer limit) {

    validateSession(context);
    Resource resource = getOwnedResource(context.userId(), resourceId);
    int resultLimit = normalizeLimit(limit, MAX_METRICS);
    String normalizedQuery = normalizeQuery(query);
    ProviderEnum provider = resource.getAccount().getConnection().getProvider();

    List<MetricToolDto> results =
        offeredMetricRepository
            .findByProviderAndServiceType(provider, resource.getResourceType())
            .stream()
            .filter(metric -> matchesMetric(metric, normalizedQuery))
            .sorted(
                Comparator.comparing(
                    OfferedMetric::getMetricName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
            .limit(resultLimit)
            .map(this::mapMetric)
            .toList();

    return Map.of(
        "resourceId",
        resourceId,
        "accountId",
        resource.getAccountId(),
        "provider",
        provider,
        "resourceType",
        resource.getResourceType(),
        RESULTS,
        results,
        COUNT,
        results.size(),
        LIMIT,
        resultLimit,
        TRUNCATED,
        results.size() >= resultLimit);
  }

  public Map<String, Object> findBillingCharges(
      AiAgentContext context, String query, Integer limit) {

    validateSession(context);
    int resultLimit = normalizeLimit(limit, MAX_CHARGES);
    String normalizedQuery = normalizeQuery(query);

    List<BillingChargeToolDto> results =
        billingService.getCharges().stream()
            .filter(charge -> matchesCharge(charge, normalizedQuery))
            .sorted(
                Comparator.comparing(
                    BillingChargeResponse::chargeId,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
            .limit(resultLimit)
            .map(
                charge ->
                    new BillingChargeToolDto(
                        charge.resourceId(),
                        charge.chargeId(),
                        charge.service(),
                        charge.provider()))
            .toList();

    return Map.of(
        RESULTS,
        results,
        COUNT,
        results.size(),
        LIMIT,
        resultLimit,
        TRUNCATED,
        results.size() >= resultLimit);
  }

  public Map<String, Object> addChartWidget(AiAgentContext context, AddChartWidgetToolDto request) {
    validateSession(context);
    validateWidgetCount(context);

    var widget = versionService.addChartWidget(context.userId(), context.sessionId(), request);
    return Map.of(
        WIDGET_ID,
        widget.widgetId(),
        WIDGET_TYPE,
        widget.widgetType(),
        DISPLAY_NAME,
        widget.displayName(),
        START_X,
        widget.startX(),
        START_Y,
        widget.startY(),
        WIDTH,
        widget.width(),
        HEIGHT,
        widget.height(),
        MESSAGE,
        "Chart widget added");
  }

  public Map<String, Object> addKpiWidget(AiAgentContext context, AddKpiWidgetToolDto request) {
    validateSession(context);
    validateWidgetCount(context);

    var widget = versionService.addKpiWidget(context.userId(), context.sessionId(), request);
    return Map.of(
        WIDGET_ID,
        widget.widgetId(),
        WIDGET_TYPE,
        widget.widgetType(),
        DISPLAY_NAME,
        widget.displayName(),
        START_X,
        widget.startX(),
        START_Y,
        widget.startY(),
        WIDTH,
        widget.width(),
        HEIGHT,
        widget.height(),
        MESSAGE,
        "KPI widget added");
  }

  public Map<String, Object> updateWidgetLayout(
      AiAgentContext context, UpdateWidgetLayoutToolDto request) {
    validateSession(context);

    var widget = versionService.updateWidgetLayout(context.userId(), context.sessionId(), request);
    return widgetSummary(widget, "Widget layout updated");
  }

  public Map<String, Object> updateChartWidget(
      AiAgentContext context, UpdateChartWidgetToolDto request) {
    validateSession(context);

    var widget = versionService.updateChartWidget(context.userId(), context.sessionId(), request);
    return widgetSummary(widget, "Chart widget updated");
  }

  public Map<String, Object> updateKpiWidget(
      AiAgentContext context, UpdateKpiWidgetToolDto request) {
    validateSession(context);

    var widget = versionService.updateKpiWidget(context.userId(), context.sessionId(), request);
    return widgetSummary(widget, "KPI widget updated");
  }

  public Map<String, Object> deleteWidget(AiAgentContext context, UUID widgetId) {
    validateSession(context);

    var widget = versionService.deleteWidget(context.userId(), context.sessionId(), widgetId);
    return Map.of(
        WIDGET_ID,
        widget.widgetId(),
        DISPLAY_NAME,
        widget.displayName(),
        MESSAGE,
        "Widget deleted");
  }

  public Map<String, Object> clearWorkingDashboard(AiAgentContext context) {
    validateSession(context);

    var draft = versionService.getWorkingDashboard(context.userId(), context.sessionId());
    int deletedCount = draft.widgets().size();
    versionService.clearWorkingDashboard(context.userId(), context.sessionId());

    return Map.of("deletedWidgetCount", deletedCount, MESSAGE, "Working dashboard cleared");
  }

  public Map<String, Object> discardWorkingDashboard(AiAgentContext context) {
    validateSession(context);
    versionService.discardWorkingDashboard(context.userId(), context.sessionId());
    return Map.of(MESSAGE, "Uncommitted dashboard changes discarded");
  }

  public Map<String, Object> updateDashboard(
      AiAgentContext context, UpdateDashboardToolDto request) {
    validateSession(context);

    var dashboard = versionService.updateDashboard(context.userId(), context.sessionId(), request);
    return Map.of(
        "title",
        dashboard.title(),
        "widgetCount",
        dashboard.widgets().size(),
        MESSAGE,
        "Dashboard metadata updated");
  }

  public Map<String, Object> commitDashboardChanges(AiAgentContext context) {
    validateSession(context);

    var response = versionService.commitWorkingDashboard(context.userId(), context.sessionId());
    return Map.of(
        "versionId",
        response.versionId(),
        "version",
        response.version(),
        MESSAGE,
        "Dashboard changes committed as a new version");
  }

  private List<CloudAccountToolDto> mapAccounts(CloudConnection connection) {
    return cloudAccountRepository.findByConnectionId(connection.getId()).stream()
        .map(
            account ->
                new CloudAccountToolDto(
                    account.getId(),
                    account.getDisplayName(),
                    connection.getProvider(),
                    account.getAccountType().name()))
        .toList();
  }

  private ResourceToolDto mapResource(Resource resource, ProviderEnum provider) {
    return new ResourceToolDto(
        resource.getId(),
        resource.getAccountId(),
        provider,
        resource.getResourceType(),
        resource.getResourceName(),
        resource.getResourceIdentifier(),
        resource.getRegion());
  }

  private boolean matchesAccount(CloudAccountToolDto account, String query) {
    if (query == null) {
      return true;
    }
    return contains(account.displayName(), query)
        || contains(account.accountType(), query)
        || String.valueOf(account.provider()).equalsIgnoreCase(query);
  }

  private boolean matchesResourceType(Resource resource, String resourceType) {
    return resourceType == null || contains(resource.getResourceType(), resourceType);
  }

  private boolean matchesResource(Resource resource, String query) {
    return query == null
        || contains(resource.getResourceName(), query)
        || contains(resource.getResourceIdentifier(), query)
        || contains(resource.getRegion(), query)
        || contains(resource.getResourceType(), query);
  }

  private boolean matchesMetric(OfferedMetric metric, String query) {
    return query == null
        || contains(metric.getMetricName(), query)
        || contains(metric.getDescription(), query)
        || contains(metric.getExpectedUnit(), query);
  }

  private boolean matchesCharge(BillingChargeResponse charge, String query) {
    return query == null
        || contains(charge.chargeId(), query)
        || contains(charge.service(), query)
        || String.valueOf(charge.provider()).equalsIgnoreCase(query);
  }

  private boolean contains(String value, String query) {
    return value != null && value.toLowerCase().contains(query);
  }

  private String normalizeQuery(String query) {
    if (query == null || query.isBlank()) {
      return null;
    }
    return query.trim().toLowerCase();
  }

  private int normalizeLimit(Integer requestedLimit, int maximum) {
    if (requestedLimit == null) {
      return maximum;
    }
    if (requestedLimit < 1 || requestedLimit > maximum) {
      throw invalid("limit must be between 1 and " + maximum);
    }
    return requestedLimit;
  }

  private Map<String, Object> widgetSummary(
      com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto widget, String message) {
    return Map.of(
        WIDGET_ID,
        widget.widgetId(),
        WIDGET_TYPE,
        widget.widgetType(),
        DISPLAY_NAME,
        widget.displayName(),
        START_X,
        widget.startX(),
        START_Y,
        widget.startY(),
        WIDTH,
        widget.width(),
        HEIGHT,
        widget.height(),
        MESSAGE,
        message);
  }

  private void validateWidgetCount(AiAgentContext context) {
    int count =
        versionService.getWorkingDashboard(context.userId(), context.sessionId()).widgets().size();
    if (count >= MAX_WIDGETS) {
      throw invalid("Dashboard cannot contain more than " + MAX_WIDGETS + " widgets");
    }
  }

  private CloudAccount getOwnedAccount(UUID userId, UUID accountId) {
    CloudAccount account =
        cloudAccountRepository
            .findById(accountId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cloud account not found"));

    if (!account.getConnection().getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cloud account not found");
    }

    return account;
  }

  private Resource getOwnedResource(UUID userId, UUID resourceId) {
    Resource resource =
        resourceRepository
            .findById(resourceId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

    if (!resource.getAccount().getConnection().getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }

    return resource;
  }

  private void validateSession(AiAgentContext context) {
    aiSessionService.getSession(context.userId(), context.sessionId());
  }

  private MetricToolDto mapMetric(OfferedMetric metric) {
    return new MetricToolDto(
        metric.getProvider(),
        metric.getServiceType(),
        metric.getMetricName(),
        metric.getIdentifierField(),
        metric.getExpectedUnit(),
        metric.getDescription());
  }

  private ResponseStatusException invalid(String message) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }
}
