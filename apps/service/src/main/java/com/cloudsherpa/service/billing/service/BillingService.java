package com.cloudsherpa.service.billing.service;

import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import com.cloudsherpa.service.billing.dto.BillingKpiRequest;
import com.cloudsherpa.service.billing.dto.BillingKpiResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BillingService {

  private final NormalizedCostsRepository normalizedCostsRepository;

  public BillingService(NormalizedCostsRepository normalizedCostsRepository) {
    this.normalizedCostsRepository = normalizedCostsRepository;
  }

  public BillingKpiResponse previewKpi(BillingKpiRequest request) {
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
