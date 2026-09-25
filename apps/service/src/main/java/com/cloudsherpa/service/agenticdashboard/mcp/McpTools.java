package com.cloudsherpa.service.agenticdashboard.mcp;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.OfferedMetric;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.entities.TypeEnum;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.OfferedMetricRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.agenticdashboard.agent.AiAgentContext;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanDto;
import com.cloudsherpa.service.agenticdashboard.dto.DashboardPlanWidgetDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.BillingChargeToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.CloudAccountToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.MetricToolDto;
import com.cloudsherpa.service.agenticdashboard.mcp.dto.ResourceToolDto;
import com.cloudsherpa.service.agenticdashboard.service.AiDashboardVersionService;
import com.cloudsherpa.service.agenticdashboard.service.AiSessionService;
import com.cloudsherpa.service.agenticdashboard.validation.DashboardPlanValidator;
import com.cloudsherpa.service.billing.dto.BillingChargeResponse;
import com.cloudsherpa.service.billing.service.BillingService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class McpTools {

  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;
  private final ResourceRepository resourceRepository;
  private final OfferedMetricRepository offeredMetricRepository;
  private final BillingService billingService;
  private final AiSessionService aiSessionService;
  private final AiDashboardVersionService versionService;
  private final DashboardPlanValidator dashboardPlanValidator;

  public McpTools(
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      ResourceRepository resourceRepository,
      OfferedMetricRepository offeredMetricRepository,
      BillingService billingService,
      AiSessionService aiSessionService,
      AiDashboardVersionService versionService,
      DashboardPlanValidator dashboardPlanValidator) {

    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.resourceRepository = resourceRepository;
    this.offeredMetricRepository = offeredMetricRepository;
    this.billingService = billingService;
    this.aiSessionService = aiSessionService;
    this.versionService = versionService;
    this.dashboardPlanValidator = dashboardPlanValidator;
  }

  public List<CloudAccountToolDto> listCloudAccounts(AiAgentContext context) {

    validateSession(context);

    List<CloudConnection> connections = cloudConnectionRepository.findByUserId(context.userId());

    return connections.stream()
        .flatMap(
            connection ->
                cloudAccountRepository.findByConnectionId(connection.getId()).stream()
                    .map(
                        account ->
                            new CloudAccountToolDto(
                                account.getId(),
                                account.getDisplayName(),
                                connection.getProvider(),
                                account.getAccountType().name())))
        .toList();
  }

  public List<ResourceToolDto> listResources(
      AiAgentContext context, UUID accountId, String resourceType) {

    validateSession(context);

    CloudAccount account = getOwnedAccount(context.userId(), accountId);

    List<Resource> resources;

    if (resourceType == null || resourceType.isBlank()) {

      resources = resourceRepository.findByAccountId(account.getId());

    } else {

      resources = resourceRepository.findByAccountIdAndResourceType(account.getId(), resourceType);
    }

    ProviderEnum provider = account.getConnection().getProvider();

    return resources.stream()
        .map(
            resource ->
                new ResourceToolDto(
                    resource.getId(),
                    resource.getAccountId(),
                    provider,
                    resource.getResourceType(),
                    resource.getResourceName(),
                    resource.getResourceIdentifier(),
                    resource.getRegion()))
        .toList();
  }

  public List<MetricToolDto> listAvailableMetrics(AiAgentContext context, UUID resourceId) {

    validateSession(context);

    Resource resource = getOwnedResource(context.userId(), resourceId);

    ProviderEnum provider = resource.getAccount().getConnection().getProvider();

    return offeredMetricRepository
        .findByProviderAndServiceType(provider, resource.getResourceType())
        .stream()
        .map(this::mapMetric)
        .toList();
  }

  public List<BillingChargeToolDto> listBillingCharges(AiAgentContext context) {

    validateSession(context);

    return billingService.getCharges().stream()
        .map(
            charge ->
                new BillingChargeToolDto(
                    charge.resourceId(), charge.chargeId(), charge.service(), charge.provider()))
        .toList();
  }

  public UUID stageDashboardVersion(AiAgentContext context, DashboardPlanDto plan) {

    validateSession(context);

    dashboardPlanValidator.validate(plan);

    validatePlanOwnership(context, plan);

    return versionService.createVersion(context.userId(), context.sessionId(), plan).getVersionId();
  }

  private void validatePlanOwnership(AiAgentContext context, DashboardPlanDto plan) {

    for (DashboardPlanWidgetDto widget : plan.widgets()) {

      if (widget.widgetType() == TypeEnum.CHART) {
        validateChartWidget(context, widget);
      }

      if (widget.widgetType() == TypeEnum.KPI) {
        validateKpiWidget(widget);
      }
    }
  }

  private void validateChartWidget(AiAgentContext context, DashboardPlanWidgetDto widget) {

    Resource resource = getOwnedResource(context.userId(), widget.resourceId());

    if (!resource.getAccountId().equals(widget.accountId())) {

      throw invalid("Chart account does not own the selected resource");
    }

    ProviderEnum provider = resource.getAccount().getConnection().getProvider();

    if (provider != widget.provider()) {
      throw invalid("Chart provider does not match the selected resource");
    }

    boolean metricExists =
        offeredMetricRepository
            .findByProviderAndServiceType(provider, resource.getResourceType())
            .stream()
            .anyMatch(metric -> metric.getMetricName().equals(widget.metricName()));

    if (!metricExists) {
      throw invalid("Metric is not available for the selected resource");
    }
  }

  private void validateKpiWidget(DashboardPlanWidgetDto widget) {

    if (widget.chargeIds() == null || widget.chargeIds().isEmpty()) {

      throw invalid("KPI charge IDs are required");
    }

    List<String> validChargeIds =
        billingService.getCharges().stream().map(BillingChargeResponse::chargeId).toList();

    if (!widget.chargeIds().stream().allMatch(validChargeIds::contains)) {

      throw invalid("One or more KPI charge IDs are not available");
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
