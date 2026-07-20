package com.cloudsherpa.service.billing.service;

import com.cloudsherpa.lib.entities.NormalizedCosts;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.billing.dto.BillingKpiRequest;
import com.cloudsherpa.service.billing.dto.BillingKpiResponse;
import com.cloudsherpa.service.billing.dto.BillingResourceResponse;
import com.cloudsherpa.service.config.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BillingService {

  private final NormalizedCostsRepository normalizedCostsRepository;
  private static final Pattern TENANT_SCHEMA_PATTERN = Pattern.compile("^tenant_[a-f0-9_]{36}$");

  @PersistenceContext private EntityManager entityManager;

  public BillingService(NormalizedCostsRepository normalizedCostsRepository) {
    this.normalizedCostsRepository = normalizedCostsRepository;
  }

  @Transactional
  public BillingKpiResponse previewKpi(BillingKpiRequest request) {
    setTenantSchemaFromContext();

    OffsetDateTime fromDate;
    OffsetDateTime toDate;

    try {
      fromDate = OffsetDateTime.parse(request.from());
      toDate = OffsetDateTime.parse(request.to());
    } catch (DateTimeParseException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Date strings must be valid timestamps");
    }

    if (fromDate.isAfter(toDate)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
    }

    List<String> resourceIds = request.resourceIds();
    BigDecimal totalCost;

    if (resourceIds == null || resourceIds.isEmpty()) {
      totalCost =
          normalizedCostsRepository.sumTotalCostBetween(
              fromDate, toDate); // when the user didn't give access to resource_id
    } else {
      totalCost =
          normalizedCostsRepository.sumTotalCostBetweenForResources(fromDate, toDate, resourceIds);
    }

    return new BillingKpiResponse(
        request.title(),
        totalCost,
        "USD", // this is the default value for AWS costs, maybe should convert it to ZAR??
        resourceIds == null ? 0 : resourceIds.size(),
        resolveTimeLabel(request.aggregation()),
        OffsetDateTime.now(ZoneOffset.UTC).toString());
  }

  @Transactional(readOnly = true)
  public List<BillingResourceResponse> getResources() {
    setTenantSchemaFromContext();

    List<NormalizedCosts> resources = normalizedCostsRepository.findDistinctByResourceId();

    List<BillingResourceResponse> response = new ArrayList<>();

    for (NormalizedCosts resource : resources) {
      response.add(
          new BillingResourceResponse(
              resource.getResourceId(), resource.getServiceName(), resource.getProvider()));
    }

    return response;
  }

  private void setTenantSchemaFromContext() {
    String tenantId = TenantContext.getCurrentTenant();

    if (tenantId == null || tenantId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No tenant context available");
    }

    String schema = normalizeTenantSchema(tenantId);
    entityManager.createNativeQuery("SET search_path TO " + schema + ", public").executeUpdate();
  }

  private String normalizeTenantSchema(String tenantId) {
    String schema = "tenant_" + tenantId.toLowerCase().replace("-", "_");

    if (!TENANT_SCHEMA_PATTERN.matcher(schema).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tenant identifier");
    }

    return schema;
  }

  private String resolveTimeLabel(String aggregation) {
    if (aggregation == null || aggregation.isBlank()) {
      return "Custom range";
    }

    return switch (aggregation.toLowerCase()) {
      case "daily" -> "Last 24 hours";
      case "weekly" -> "Last 7 days";
      case "monthly" -> "Last 30 days";
      default -> "Custom range";
    };
  }
}
