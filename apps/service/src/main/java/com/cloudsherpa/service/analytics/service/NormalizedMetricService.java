package com.cloudsherpa.service.analytics.service;

import com.cloudsherpa.service.analytics.entities.NormalizedMetrics;
import com.cloudsherpa.service.analytics.projections.ResourceNames;
import com.cloudsherpa.service.analytics.repository.NormalizedMetricsRepository;
import com.cloudsherpa.service.analytics.repository.ResourceRepository;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NormalizedMetricService {
  private final NormalizedMetricsRepository normalizedMetricsRepository;
  private final ResourceRepository resourceRepository;

  NormalizedMetricService(
      NormalizedMetricsRepository normalizedMetricsRepository,
      ResourceRepository resourceRepository) {
    this.normalizedMetricsRepository = normalizedMetricsRepository;
    this.resourceRepository = resourceRepository;
  }

  public List<NormalizedMetrics> fetchHistoricalData(String from, String to) throws Exception {

    OffsetDateTime parsedFromDate;
    OffsetDateTime parsedToDate;

    try {
      parsedFromDate = OffsetDateTime.parse(from);
      parsedToDate = OffsetDateTime.parse(to);
    } catch (DateTimeParseException e) {
      // Still need to log for server-side visibility
      throw new Exception("Date Strings do not conform to ISO-8601 standard");
    }

    if (parsedFromDate.isAfter(parsedToDate)) {
      // from after to
      throw new Exception("Invalid interval");
    }

    return normalizedMetricsRepository.findByRecordedAtBetween(parsedFromDate, parsedToDate);
  }

  public Map<String, String> fetchResourceNames() throws ResponseStatusException {
    List<ResourceNames> resourceNames = resourceRepository.findResourceNames();

    if (resourceNames.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No resources found");
    }

    Map<String, String> resourceIdNameMap = new HashMap<>();

    for (ResourceNames resourceName : resourceNames) {
      resourceIdNameMap.put(resourceName.getId().toString(), resourceName.getResourceType());
    }

    return resourceIdNameMap;
  }
}
