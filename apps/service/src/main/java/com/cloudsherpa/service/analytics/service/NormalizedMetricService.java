package com.cloudsherpa.service.analytics.service;

import com.cloudsherpa.lib.projections.AggregatedMetric;
import com.cloudsherpa.lib.projections.ResourceNames;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
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

  public List<AggregatedMetric> fetchHistoricalData(String from, String to, String interval)
      throws ResponseStatusException {

    OffsetDateTime parsedFromDate;
    OffsetDateTime parsedToDate;

    try {
      parsedFromDate = OffsetDateTime.parse(from);
      parsedToDate = OffsetDateTime.parse(to);
    } catch (DateTimeParseException e) {
      // Still need to log for server-side visibility
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Date Strings do not conform to ISO-8601 standard");
    }

    if (parsedFromDate.isAfter(parsedToDate)) {
      // from after to
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid interval");
    }

    String bucketWidth;

    switch (interval) {
      case "daily":
        bucketWidth = "1 day";
        break;
      case "weekly":
        bucketWidth = "1 week";
        break;
      case "monthly":
        bucketWidth = "1 month";
        break;
      default:
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invalid interval. Supported values: daily, weekly, monthly");
    }

    return normalizedMetricsRepository.findAggregatedMetricsByPeriod(
        parsedFromDate, parsedToDate, bucketWidth);
  }

  public Map<String, String> fetchResourceNames() {
    List<ResourceNames> resourceNames = resourceRepository.findResourceNames();

    Map<String, String> resourceIdNameMap = new HashMap<>();

    for (ResourceNames resourceName : resourceNames) {
      resourceIdNameMap.put(resourceName.getId().toString(), resourceName.getResourceType());
    }

    return resourceIdNameMap;
  }
}
