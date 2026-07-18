package com.cloudsherpa.service.billing.service;

import com.cloudsherpa.lib.entities.CloudAccount;
import com.cloudsherpa.lib.entities.CloudConnection;
import com.cloudsherpa.lib.entities.Resource;
import com.cloudsherpa.lib.repositories.CloudAccountRepository;
import com.cloudsherpa.lib.repositories.CloudConnectionRepository;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.billing.dto.BillingConnectionResponse;
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
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BillingService {

  private final NormalizedCostsRepository normalizedCostsRepository;
  private final CloudConnectionRepository cloudConnectionRepository;
  private final CloudAccountRepository cloudAccountRepository;
  private final ResourceRepository resourceRepository;
  private static final Pattern TENANT_SCHEMA_PATTERN = Pattern.compile("^tenant_[a-f0-9_]{36}$");

  @PersistenceContext private EntityManager entityManager;

  public BillingService(
      NormalizedCostsRepository normalizedCostsRepository,
      CloudConnectionRepository cloudConnectionRepository,
      CloudAccountRepository cloudAccountRepository,
      ResourceRepository resourceRepository) {
    this.normalizedCostsRepository = normalizedCostsRepository;
    this.cloudConnectionRepository = cloudConnectionRepository;
    this.cloudAccountRepository = cloudAccountRepository;
    this.resourceRepository = resourceRepository;
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

    List<UUID> resourceIds = request.resourceIds();
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

  public List<BillingConnectionResponse> getConnections(UUID userId) {
    List<CloudConnection> connections = cloudConnectionRepository.findByUserId(userId);
    List<BillingConnectionResponse> response = new ArrayList<>();

    for (CloudConnection connection : connections) {
      List<CloudAccount> accounts = cloudAccountRepository.findByConnectionId(connection.getId());

      for (CloudAccount account : accounts) {
        response.add(
            new BillingConnectionResponse(
                connection.getId(), account.getDisplayName(), connection.getProvider().name()));
      }
    }

    return response;
  }

  @Transactional(readOnly = true)
  public List<BillingResourceResponse> getResources(UUID userId, UUID connectionId, String search) {
    setTenantSchemaFromContext();

    List<CloudConnection> connections = cloudConnectionRepository.findByUserId(userId);
    String normalizedSearch = normalizeSearch(search);

    List<BillingResourceResponse> resources = new ArrayList<>();

    for (CloudConnection connection : connections) {
      if (shouldSkipConnection(connectionId, connection.getId())) {
        continue;
      }

      List<CloudAccount> accounts = cloudAccountRepository.findByConnectionId(connection.getId());

      for (CloudAccount account : accounts) {
        List<Resource> accountResources = resourceRepository.findByAccountId(account.getId());

        for (Resource resource : accountResources) {
          if (!matchesSearch(resource, normalizedSearch)) {
            continue;
          }

          resources.add(
              new BillingResourceResponse(
                  resource.getId(),
                  resource.getResourceName(),
                  deriveService(resource.getResourceType()),
                  connection.getProvider().name(),
                  connection.getId(),
                  account.getDisplayName()));
        }
      }
    }

    return resources;
  }

  private boolean shouldSkipConnection(UUID requestedConnectionId, UUID currentConnectionId) {
    return requestedConnectionId != null && !requestedConnectionId.equals(currentConnectionId);
  }

  private String normalizeSearch(String search) {
    return search == null ? "" : search.trim().toLowerCase();
  }

  private boolean matchesSearch(Resource resource, String normalizedSearch) {
    if (normalizedSearch.isBlank()) {
      return true;
    }

    String resourceName = resource.getResourceName();
    String resourceId = resource.getId().toString();

    boolean matchesName =
        resourceName != null && resourceName.toLowerCase().contains(normalizedSearch);
    boolean matchesId = resourceId.contains(normalizedSearch);

    return matchesName || matchesId;
  }

  private String deriveService(String resourceType) {
    if (resourceType == null || resourceType.isBlank()) {
      return "Unknown";
    }

    // Resource type strings look like AWS::EC2::Volume.
    // This pulls out the middle piece, e.g. EC2.
    String[] parts = resourceType.split("::");
    if (parts.length >= 2) {
      return parts[1];
    }

    // If the format is unexpected, just return the raw value.
    return resourceType;
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
