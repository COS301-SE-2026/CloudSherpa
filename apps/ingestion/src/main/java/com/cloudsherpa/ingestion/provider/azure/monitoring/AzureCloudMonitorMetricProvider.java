package com.cloudsherpa.ingestion.provider.azure.monitoring;

import com.azure.core.util.Context;
import com.azure.monitor.query.metrics.MetricsClient;
import com.azure.monitor.query.metrics.models.AggregationType;
import com.azure.monitor.query.metrics.models.MetricResult;
import com.azure.monitor.query.metrics.models.MetricValue;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesResult;
import com.azure.monitor.query.metrics.models.MetricsQueryResult;
import com.azure.monitor.query.metrics.models.MetricsQueryTimeInterval;
import com.azure.monitor.query.metrics.models.TimeSeriesElement;
import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.Instance;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.azure.factory.AzureClientFactory;
import com.cloudsherpa.ingestion.provider.monitoring.CloudMonitoringMetricProvider;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Azure Monitor Metrics Batch API restrictions used by this provider: Maximum 50 resource IDs per
 * request. All resources in a batch must be in the same subscription. All resources in a batch must
 * be in the same Azure region. All resources in a batch must be the same resource type. Maximum 20
 * metric names per request.
 */
@Component
public class AzureCloudMonitorMetricProvider implements CloudMonitoringMetricProvider {

  private static final int MAX_RESOURCES_PER_REQUEST = 50;
  private static final int MAX_METRICS_PER_REQUEST = 20;
  private static final List<AggregationType> AGGREGATIONS =
      Collections.singletonList(AggregationType.AVERAGE);

  @Override
  public List<UsageRecordModel> collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request) {

    validateRequest(accountScope, request);

    QueryContext queryContext = createQueryContext(accountScope, request);

    List<UsageRecordModel> results = new ArrayList<>();

    for (ServiceScope serviceScope : accountScope.getServiceScopes()) {
      if (!hasMetricsToCollect(serviceScope)) {
        continue;
      }

      results.addAll(collectServiceMetrics(request.getCredentials(), serviceScope, queryContext));
    }
    return results;
  }

  private List<UsageRecordModel> collectServiceMetrics(
      CloudCredentials credentials, ServiceScope serviceScope, QueryContext queryContext) {

    Map<String, List<AzureResource>> resourcesByRegion = groupResourcesByRegion(serviceScope);

    List<List<Metric>> metricBatches =
        partition(serviceScope.getMetrics(), MAX_METRICS_PER_REQUEST);

    List<UsageRecordModel> results = new ArrayList<>();

    for (Map.Entry<String, List<AzureResource>> regionEntry : resourcesByRegion.entrySet()) {
      MetricsClient client =
          AzureClientFactory.createMetricsClient(
              credentials, regionEntry.getValue().getFirst().region);
      results.addAll(
          collectRegionMetrics(
              client,
              serviceScope,
              regionEntry.getKey(),
              regionEntry.getValue(),
              metricBatches,
              queryContext));
    }
    return results;
  }

  private List<UsageRecordModel> collectRegionMetrics(
      MetricsClient client,
      ServiceScope serviceScope,
      String region,
      List<AzureResource> resources,
      List<List<Metric>> metricBatches,
      QueryContext queryContext) {

    List<UsageRecordModel> results = new ArrayList<>();

    List<List<AzureResource>> resourceBatches = partition(resources, MAX_RESOURCES_PER_REQUEST);

    for (List<AzureResource> resourceBatch : resourceBatches) {
      for (List<Metric> metricBatch : metricBatches) {
        QueryBatch batch =
            new QueryBatch(resourceBatch, metricBatch, serviceScope.getName(), region);

        MetricsQueryResourcesResult response = queryMetrics(client, batch, queryContext);
        results.addAll(processResponse(response, batch, serviceScope, queryContext));
      }
    }
    return results;
  }

  private Map<String, List<AzureResource>> groupResourcesByRegion(ServiceScope serviceScope) {

    Map<String, List<AzureResource>> resourcesByRegion = new LinkedHashMap<>();

    for (InstanceScope instanceScope : serviceScope.getInstances()) {
      if (instanceScope == null || instanceScope.getInstances() == null) {
        continue;
      }

      for (Instance instance : instanceScope.getInstances()) {
        if (!isValidInstance(instance)) {
          continue;
        }

        String region = instance.getRegion();

        if (region == null || region.isBlank()) {
          throw new IllegalArgumentException(
              "Azure resource is missing its region: " + instance.getIdentifier());
        }

        AzureResource resource = new AzureResource(instance.getIdentifier(), region);

        resourcesByRegion.computeIfAbsent(region, ignored -> new ArrayList<>()).add(resource);
      }
    }
    return resourcesByRegion;
  }

  private MetricsQueryResourcesResult queryMetrics(
      MetricsClient client, QueryBatch batch, QueryContext queryContext) {
    List<String> resourceIds = batch.resources().stream().map(AzureResource::resourceId).toList();

    List<String> metricNames = batch.metrics().stream().map(Metric::getName).toList();

    MetricsQueryResourcesOptions options =
        new MetricsQueryResourcesOptions()
            .setTimeInterval(
                new MetricsQueryTimeInterval(
                    toOffsetDateTime(queryContext.from()), toOffsetDateTime(queryContext.to())))
            .setGranularity(Duration.ofSeconds(queryContext.period()))
            .setAggregations(AGGREGATIONS);

    return client
        .queryResourcesWithResponse(
            resourceIds, metricNames, batch.metricNamespace(), options, Context.NONE)
        .getValue();
  }

  private List<UsageRecordModel> processResponse(
      MetricsQueryResourcesResult response,
      QueryBatch batch,
      ServiceScope serviceScope,
      QueryContext queryContext) {

    List<UsageRecordModel> results = new ArrayList<>();

    if (response == null || response.getMetricsQueryResults() == null) {
      return results;
    }

    Map<String, AzureResource> resourcesById =
        batch.resources().stream()
            .collect(
                Collectors.toMap(
                    AzureResource::resourceId,
                    resource -> resource,
                    (first, second) -> first,
                    LinkedHashMap::new));

    for (MetricsQueryResult resourceResult : response.getMetricsQueryResults()) {
      if (resourceResult == null || resourceResult.getResourceId() == null) {
        continue;
      }

      AzureResource resource = resourcesById.get(resourceResult.getResourceId());

      if (resource != null) {
        results.addAll(processResourceResult(resourceResult, resource, serviceScope, queryContext));
      }
    }
    return results;
  }

  private List<UsageRecordModel> processResourceResult(
      MetricsQueryResult resourceResult,
      AzureResource resource,
      ServiceScope serviceScope,
      QueryContext queryContext) {

    if (resourceResult.getMetrics() == null) {
      return new ArrayList<>();
    }

    List<UsageRecordModel> results = new ArrayList<>();

    for (MetricResult metricResult : resourceResult.getMetrics()) {
      processMetricResult(metricResult, resource, serviceScope, queryContext, results);
    }

    return results;
  }

  private void processMetricResult(
      MetricResult metricResult,
      AzureResource resource,
      ServiceScope serviceScope,
      QueryContext queryContext,
      List<UsageRecordModel> results) {

    if (metricResult == null || metricResult.getTimeSeries() == null) {
      return;
    }

    Metric requestedMetric =
        findRequestedMetric(queryContext.metrics(), metricResult.getMetricName());

    for (TimeSeriesElement timeSeries : metricResult.getTimeSeries()) {
      processTimeSeries(
          metricResult, timeSeries, resource, serviceScope, requestedMetric, queryContext, results);
    }
  }

  private void processTimeSeries(
      MetricResult metricResult,
      TimeSeriesElement timeSeries,
      AzureResource resource,
      ServiceScope serviceScope,
      Metric requestedMetric,
      QueryContext queryContext,
      List<UsageRecordModel> results) {

    if (timeSeries == null || timeSeries.getValues() == null) {
      return;
    }

    Map<String, String> dimensions = getDimensions(timeSeries);

    for (MetricValue metricValue : timeSeries.getValues()) {
      UsageRecordModel usage =
          createUsageRecord(
              new RecordContext(
                  metricResult, metricValue, resource, serviceScope, requestedMetric, dimensions),
              queryContext);

      addIfPresent(results, usage);
    }
  }

  private void addIfPresent(List<UsageRecordModel> results, UsageRecordModel usage) {

    if (usage != null) {
      results.add(usage);
    }
  }

  private UsageRecordModel createUsageRecord(
      RecordContext recordContext, QueryContext queryContext) {

    MetricValue metricValue = recordContext.metricValue();

    if (metricValue == null || metricValue.getTimeStamp() == null) {
      return null;
    }

    Double value = extractValue(metricValue);

    if (value == null) {
      return null;
    }

    Instant timestamp = metricValue.getTimeStamp().toInstant();

    MetricResult metricResult = recordContext.metricResult();

    UsageRecordModel usage = new UsageRecordModel();

    usage.setProvider(
        queryContext.accountScope().getProvider() != null
            ? queryContext.accountScope().getProvider()
            : "AZURE");

    usage.setAccountId(queryContext.accountScope().getAccountId());
    usage.setSubscriptionId(queryContext.accountScope().getSubscriptionId());
    usage.setServiceName(recordContext.serviceScope().getName());
    usage.setMetricName(metricResult.getMetricName());
    usage.setResourceId(recordContext.resource().resourceId());
    usage.setResourceType(getResourceType(metricResult, recordContext.serviceScope()));
    usage.setRegion(recordContext.resource().region());
    usage.setUnit(getUnit(metricResult, recordContext.requestedMetric()));
    usage.setValue(value);
    usage.setTimestamp(timestamp);
    usage.setIngestionTimestamp(Instant.now());
    usage.setRecordId(UUID.randomUUID());
    usage.setIngestionId(queryContext.ingestionId());
    usage.setDimensions(recordContext.dimensions());
    usage.setSource("AzureMonitor");
    usage.setPeriodStart(timestamp);
    usage.setPeriodEnd(timestamp.plusSeconds(queryContext.period()));

    return usage;
  }

  /**
   * Gets the average value returned by Azure. Average is requested explicitly. The fallbacks
   * protect against metrics that do not return an average value.
   */
  private Double extractValue(MetricValue metricValue) {

    if (metricValue.getAverage() != null) {
      return metricValue.getAverage();
    }

    if (metricValue.getTotal() != null) {
      return metricValue.getTotal();
    }

    if (metricValue.getMaximum() != null) {
      return metricValue.getMaximum();
    }

    if (metricValue.getMinimum() != null) {
      return metricValue.getMinimum();
    }

    if (metricValue.getCount() != null) {
      return metricValue.getCount();
    }

    return null;
  }

  // Finds the request metric definition corresponding to an Azure metric.
  private Metric findRequestedMetric(List<Metric> metrics, String metricName) {

    if (metrics == null || metricName == null) {
      return null;
    }

    return metrics.stream()
        .filter(Objects::nonNull)
        .filter(metric -> metricName.equals(metric.getName()))
        .findFirst()
        .orElse(null);
  }

  // Gets metric unit from Azure response, fallback: provided request metric
  // definition.
  private String getUnit(MetricResult metricResult, Metric requestedMetric) {

    if (metricResult.getUnit() != null) {
      return metricResult.getUnit().toString();
    }

    if (requestedMetric != null) {
      return requestedMetric.getUnit();
    }
    return null;
  }

  private String getResourceType(MetricResult metricResult, ServiceScope serviceScope) {

    if (metricResult.getResourceType() != null && !metricResult.getResourceType().isBlank()) {
      return metricResult.getResourceType();
    }
    return serviceScope.getName();
  }

  private Map<String, String> getDimensions(TimeSeriesElement timeSeries) {

    if (timeSeries.getMetadata() == null) {
      return Collections.emptyMap();
    }
    return new LinkedHashMap<>(timeSeries.getMetadata());
  }

  // Creates the context used by all queries for one ingestion request.
  private QueryContext createQueryContext(
      AccountScope accountScope, IngestionRequestEvent request) {

    return new QueryContext(
        accountScope,
        request.getFrom(),
        request.getTo(),
        request.getPeriod(),
        UUID.randomUUID().toString(),
        Collections.emptyList());
  }

  // Converts an Instant to the OffsetDateTime expected by Azure.
  private OffsetDateTime toOffsetDateTime(Instant instant) {

    if (instant == null) {
      throw new IllegalArgumentException("Metric request timestamp cannot be null");
    }
    return instant.atOffset(ZoneOffset.UTC);
  }

  // Splits a list into batches of the supplied maximum size.
  private <T> List<List<T>> partition(List<T> values, int batchSize) {
    List<List<T>> batches = new ArrayList<>();

    if (values == null || values.isEmpty()) {
      return batches;
    }

    for (int start = 0; start < values.size(); start += batchSize) {
      int end = Math.min(start + batchSize, values.size());

      batches.add(new ArrayList<>(values.subList(start, end)));
    }
    return batches;
  }

  // Checks whether an Instance contains enough information to query Azure.
  private boolean isValidInstance(Instance instance) {
    return instance != null
        && instance.getIdentifier() != null
        && !instance.getIdentifier().isBlank();
  }

  // Checks whether a service contains resources and metrics to collect.
  private boolean hasMetricsToCollect(ServiceScope serviceScope) {

    return serviceScope != null
        && serviceScope.getInstances() != null
        && !serviceScope.getInstances().isEmpty()
        && serviceScope.getMetrics() != null
        && !serviceScope.getMetrics().isEmpty()
        && serviceScope.getName() != null
        && !serviceScope.getName().isBlank();
  }

  // Validates the Azure metric ingestion request.
  private void validateRequest(AccountScope accountScope, IngestionRequestEvent request) {

    if (accountScope == null) {
      throw new IllegalArgumentException("Account scope cannot be null");
    }

    if (request == null) {
      throw new IllegalArgumentException("Ingestion request cannot be null");
    }

    validateCredentials(request);

    if (accountScope.getSubscriptionId() == null || accountScope.getSubscriptionId().isBlank()) {
      throw new IllegalArgumentException("Azure subscriptionId is required");
    }

    if (request.getFrom() == null || request.getTo() == null) {
      throw new IllegalArgumentException("Metric request from/to timestamps are required");
    }

    if (!request.getFrom().isBefore(request.getTo())) {
      throw new IllegalArgumentException("Metric request 'from' must be before 'to'");
    }

    if (request.getPeriod() <= 0) {
      throw new IllegalArgumentException("Metric period must be > 0");
    }
  }

  private void validateCredentials(IngestionRequestEvent request) {

    if (request.getCredentials() == null) {
      throw new IllegalArgumentException(
          "Azure credentials are required for Azure metric ingestion");
    }

    if (isBlank(request.getCredentials().getTenantId())) {
      throw new IllegalArgumentException("Azure tenantId is required");
    }

    if (isBlank(request.getCredentials().getClientId())) {
      throw new IllegalArgumentException("Azure clientId is required");
    }

    if (isBlank(request.getCredentials().getClientSecret())) {
      throw new IllegalArgumentException("Azure clientSecret is required");
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private record QueryContext(
      AccountScope accountScope,
      Instant from,
      Instant to,
      int period,
      String ingestionId,
      List<Metric> metrics) {}

  private record QueryBatch(
      List<AzureResource> resources, List<Metric> metrics, String metricNamespace, String region) {}

  private record RecordContext(
      MetricResult metricResult,
      MetricValue metricValue,
      AzureResource resource,
      ServiceScope serviceScope,
      Metric requestedMetric,
      Map<String, String> dimensions) {}

  private record AzureResource(String resourceId, String region) {}
}
