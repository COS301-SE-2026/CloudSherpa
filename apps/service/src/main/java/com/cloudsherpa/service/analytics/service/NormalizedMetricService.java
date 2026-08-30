package com.cloudsherpa.service.analytics.service;

import com.cloudsherpa.lib.dtos.ResourceMetricEntry;
import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.lib.entities.NormalizedMetrics;
import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.projections.AggregatedMetric;
import com.cloudsherpa.lib.projections.ResourceNames;
import com.cloudsherpa.lib.repositories.NormalizedMetricsRepository;
import com.cloudsherpa.lib.repositories.ResourceRepository;
import com.cloudsherpa.service.analytics.dto.DownsampledSeriesRequestDto;
import com.cloudsherpa.service.analytics.dto.MetricDto;
import com.cloudsherpa.service.analytics.dto.ResourceMetricHistoricalResponseDto;
import com.cloudsherpa.service.analytics.dto.ResourceMetricsGroupDto;
import com.cloudsherpa.service.analytics.model.ResourceMetric;
import com.cloudsherpa.service.metrics.MetricDisplayNameMapper;
import com.cloudsherpa.service.metrics.ResourceProviderResolver;
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
  private final MetricDisplayNameMapper metricMapper;
  private final ResourceProviderResolver resourceProviderResolver;

  private final Logger logger = LoggerFactory.getLogger(NormalizedMetricService.class);

  NormalizedMetricService(
      NormalizedMetricsRepository normalizedMetricsRepository,
      ResourceRepository resourceRepository,
      MetricDisplayNameMapper metricMapper,
      ResourceProviderResolver resourceProviderResolver) {
    this.normalizedMetricsRepository = normalizedMetricsRepository;
    this.resourceRepository = resourceRepository;
    this.metricMapper = metricMapper;
    this.resourceProviderResolver = resourceProviderResolver;
  }

  public List<MetricDto> fetchHistoricalData(String from, String to, String interval) {
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

    List<AggregatedMetric> rawMetrics =
        normalizedMetricsRepository.findAggregatedMetricsByPeriod(
            parsedFromDate, parsedToDate, bucketWidth);

    return rawMetrics.stream()
        .map(
            m ->
                new MetricDto(
                    m.getResourceId(),
                    m.getMetricType(),
                    metricMapper.toDisplayName(m.getMetricName()),
                    m.getMetricValue(),
                    m.getUnit(),
                    m.getPeriodStart().atOffset(ZoneOffset.UTC),
                    m.getPeriodEnd().atOffset(ZoneOffset.UTC),
                    m.getSampleCount()))
        .toList();
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

    ProviderEnum provider = resourceProviderResolver.resolveProvider(resourceId);

    String canonMetricName = metricMapper.toCanonicalName(provider.toString(), metricType);

    List<TimestampedNumericDataPoint> fetchedResourceMetrics =
        normalizedMetricsRepository.getTimestampedMetricValuesAfterDate(
            resourceId, canonMetricName, fromInstant);

    if (fetchedResourceMetrics.isEmpty()) {
      logger.info(
          "Lookup failed for resource {}, metric {} and data {}",
          resourceId,
          canonMetricName,
          from);
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

  public List<NormalizedMetrics> fetchDownsampledSeries(DownsampledSeriesRequestDto request) {
    return normalizedMetricsRepository.getDownsampledNormalizedMetrics(
        request.resourceId(), request.metricName(), request.from(), request.to());
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
                  metricMapper.toDisplayName(distinctResourceMetric.metricName()),
                  distinctResourceMetric.metricType()));
    }

    metricsByResourceId.forEach(
        (resourceId, metrics) ->
            groupedResourceMetrics.add(new ResourceMetricsGroupDto(resourceId, metrics)));
    return groupedResourceMetrics;
  }
}
