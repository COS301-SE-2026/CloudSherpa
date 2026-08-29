package com.cloudsherpa.service.analytics.service;

import com.cloudsherpa.lib.dtos.ResourceMetricEntry;
import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.projections.AggregatedMetric;
import com.cloudsherpa.lib.projections.ResourceNames;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.dto.ResourceMetricHistoricalResponseDto;
import com.cloudsherpa.service.analytics.dto.ResourceMetricsGroupDto;
import com.cloudsherpa.service.analytics.model.ResourceMetric;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NormalizedMetricService {
  private final NormalizedMetricsRepository normalizedMetricsRepository;
  private final ResourceRepository resourceRepository;

  private final Logger logger = LoggerFactory.getLogger(NormalizedMetricService.class);

  NormalizedMetricService(
      NormalizedMetricsRepository normalizedMetricsRepository,
      ResourceRepository resourceRepository) {
    this.normalizedMetricsRepository = normalizedMetricsRepository;
    this.resourceRepository = resourceRepository;
  }

  public List<AggregatedMetric> fetchHistoricalData(String from, String to, String interval) {
    OffsetDateTime parsedFromDate;
    OffsetDateTime parsedToDate;
    String normalizedInterval = interval.toLowerCase(Locale.ROOT);

    try {
      parsedFromDate = OffsetDateTime.parse(from);
      parsedToDate = OffsetDateTime.parse(to);
    } catch (DateTimeParseException e) {
      // Still need to log for server-side visibility
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Date Strings do not conform to ISO-8601 standard");
    }

    if (parsedFromDate.isAfter(parsedToDate)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
    }

    String bucketWidth;

    switch (normalizedInterval) {
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

  public ResourceMetricHistoricalResponseDto fetchHistoricalDataForResourceMetric(
      UUID resourceId, String metricType, OffsetDateTime from) {

    ZoneOffset offset = from.getOffset();
    Instant fromInstant = from.toInstant();
    List<TimestampedNumericDataPoint> fetchedResourceMetrics =
        normalizedMetricsRepository.getTimestampedMetricValuesAfterDate(
            resourceId, metricType, fromInstant);

    if (fetchedResourceMetrics.isEmpty()) {
      logger.info(
          "Lookup failed for resource {}, metric {} and data {}", resourceId, metricType, from);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No resource metrics found");
    }

    List<BigDecimal> values = new ArrayList<>();
    List<OffsetDateTime> timestamps = new ArrayList<>();

    for (TimestampedNumericDataPoint timestampedNumericDataPoint : fetchedResourceMetrics) {
      values.addLast(timestampedNumericDataPoint.value());
      timestamps.addLast(timestampedNumericDataPoint.timestamp().atOffset(offset));
    }

    return new ResourceMetricHistoricalResponseDto(values, timestamps);
  }

  public List<ResourceMetricsGroupDto> fetchResourceMetrics() {

    List<ResourceMetricEntry> distinctResourceMetrics =
        normalizedMetricsRepository.findDistinctResourceMetrics();

    List<ResourceMetricsGroupDto> groupedResourceMetrics = new ArrayList<>();

    Map<UUID, List<ResourceMetric>> metricsByResourceId = new HashMap<>();

    for (ResourceMetricEntry distinctResourceMetric : distinctResourceMetrics) {
      metricsByResourceId
          .computeIfAbsent(distinctResourceMetric.resourceId(), resourceId -> new ArrayList<>())
          .add(
              new ResourceMetric(
                  distinctResourceMetric.metricName(), distinctResourceMetric.metricType()));
    }

    metricsByResourceId.forEach(
        (resourceId, metrics) ->
            groupedResourceMetrics.add(new ResourceMetricsGroupDto(resourceId, metrics)));
    return groupedResourceMetrics;
  }
}
