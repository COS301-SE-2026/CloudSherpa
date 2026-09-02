package com.cloudsherpa.ingestion.provider.gcp.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.Instance;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.gcp.factory.GcpClientFactory;
import com.cloudsherpa.ingestion.provider.monitoring.CloudMonitoringMetricProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.monitoring.v3.Aggregation;
import com.google.monitoring.v3.ListTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.protobuf.Duration;
import com.google.protobuf.util.Timestamps;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GcpCloudMonitoringMetricProvider implements CloudMonitoringMetricProvider {
  private static final Logger logger =
      LoggerFactory.getLogger(GcpCloudMonitoringMetricProvider.class);

  private MetricServiceClient buildClient(CloudCredentials credentials) throws IOException {

    GoogleCredentials googleCredentials = GcpClientFactory.credentials(credentials);

    MetricServiceSettings settings =
        MetricServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
            .build();

    return MetricServiceClient.create(settings);
  }

  private MetricFilter buildFilter(
      String resourceLabel,
      String resourceType,
      Metric metric,
      String resourceId,
      String resourceRegion) {

    StringBuilder filter = new StringBuilder();

    filter.append("resource.type=\"").append(resourceType).append("\" ");

    filter
        .append("AND resource.labels.\"")
        .append(resourceLabel)
        .append("\"=\"")
        .append(resourceId)
        .append("\" ");

    filter.append("AND metric.type=\"").append(metric.getName()).append("\"");

    return new MetricFilter(
        resourceType, resourceId, resourceLabel, filter.toString(), metric, resourceRegion);
  }

  private List<MetricFilter> processServiceScope(ServiceScope scope) {
    List<MetricFilter> filters = new ArrayList<>();
    for (InstanceScope instance : scope.getInstances()) {
      filters.addAll(processInstanceScope(scope, instance));
    }
    return filters;
  }

  private List<MetricFilter> processInstanceScope(ServiceScope scope, InstanceScope instance) {
    List<MetricFilter> filters = new ArrayList<>();
    for (Instance instanceDetail : instance.getInstances()) {
      String instanceValue = instanceDetail.getIdentifier();
      for (Metric metric : scope.getMetrics()) {
        MetricFilter filter =
            buildFilter(
                instance.getIdentifierName(),
                scope.getName(),
                metric,
                instanceValue,
                instanceDetail.getRegion());
        filters.add(filter);
      }
    }
    return filters;
  }

  private Double extractValue(Point point) {

    switch (point.getValue().getValueCase()) {
      case DOUBLE_VALUE:
        return point.getValue().getDoubleValue();

      case INT64_VALUE:
        return (double) point.getValue().getInt64Value();

      case BOOL_VALUE:
        return point.getValue().getBoolValue() ? 1D : 0D;

      default:
        return 0D;
    }
  }

  private List<UsageRecordModel> processSeries(
      TimeSeries series,
      Metric metric,
      String resourceId,
      String serviceType,
      String resourceType,
      String region) {
    List<UsageRecordModel> results = new ArrayList<>();

    for (Point point : series.getPointsList()) {

      UsageRecordModel usage = new UsageRecordModel();
      usage.setServiceName(serviceType);
      usage.setMetricName(metric.getName());
      usage.setResourceType(resourceType);
      usage.setResourceId(resourceId);
      usage.setUnit(metric.getUnit());
      usage.setRegion(region);
      usage.setTimestamp(Instant.ofEpochSecond(point.getInterval().getEndTime().getSeconds()));
      usage.setIngestionTimestamp(Instant.now());
      usage.setPeriodEnd(Instant.ofEpochSecond(point.getInterval().getEndTime().getSeconds()));
      usage.setPeriodStart(Instant.ofEpochSecond(point.getInterval().getStartTime().getSeconds()));
      usage.setValue(extractValue(point));
      usage.setDimensions(
          series
              .getResource()
              .getLabelsMap()); // region is contained within this but may not always
      // have the same label, e.g. "zone", "region",
      // "location" etc.

      results.add(usage);
    }

    return results;
  }

  @Override
  public List<UsageRecordModel> collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request) {
    String ingestionId = UUID.randomUUID().toString();
    MetricServiceClient client = null;
    try {
      client = buildClient(request.getCredentials());
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Invalid account credentials provided for GCP usage metric ingestion");
    }
    String projectName = "projects/" + request.getCredentials().getProjectId();

    Aggregation aggregation =
        Aggregation.newBuilder()
            .setAlignmentPeriod(Duration.newBuilder().setSeconds(request.getPeriod()).build())
            .setPerSeriesAligner(Aggregation.Aligner.ALIGN_MEAN)
            .build();

    TimeInterval interval =
        TimeInterval.newBuilder()
            .setStartTime(Timestamps.fromMillis(request.getFrom().toEpochMilli()))
            .setEndTime(Timestamps.fromMillis(request.getTo().toEpochMilli()))
            .build();
    List<MetricFilter> requestFilters = new ArrayList<>();
    for (ServiceScope scope :
        accountScope.getServiceScopes()) { // we build filters per metric and return all of them
      requestFilters.addAll(processServiceScope(scope));
    }

    List<UsageRecordModel> results = new ArrayList<>();

    for (MetricFilter metricFilter : requestFilters) {

      ListTimeSeriesRequest metricRequest =
          ListTimeSeriesRequest.newBuilder()
              .setName(projectName)
              .setFilter(metricFilter.filter())
              .setInterval(interval)
              .setAggregation(aggregation)
              .setView(ListTimeSeriesRequest.TimeSeriesView.FULL)
              .build();
      logger.info(
          "GCP querying metric={} resource={} from={} to={} filter={}",
          metricFilter.metrics().getName(),
          metricFilter.resourceId(),
          request.getFrom(),
          request.getTo(),
          metricFilter.filter());
      Iterable<TimeSeries> timeSeries = client.listTimeSeries(metricRequest).iterateAll();
      List<TimeSeries> seriesList = new ArrayList<>();

      for (TimeSeries oneSeries : timeSeries) {
        seriesList.add(oneSeries);
      }

      int pointCount = seriesList.stream().mapToInt(TimeSeries::getPointsCount).sum();

      logger.info(
          "GCP query completed metric={} series={} points={}",
          metricFilter.metrics().getName(),
          seriesList.size(),
          pointCount);

      timeSeries.forEach(
          series ->
              results.addAll(
                  processSeries(
                      series,
                      metricFilter.metrics(),
                      metricFilter.resourceId(),
                      metricFilter.serviceType(),
                      metricFilter.resourceType(),
                      metricFilter.region())));
    }

    client.close();
    for (UsageRecordModel result : results) {
      result.setAccountId(accountScope.getAccountId());
      result.setProjectId(accountScope.getAccountId());
      result.setIngestionId(ingestionId);
      result.setProvider("GCP");
      result.setSource("GCPMonitoringService");
      result.setRecordId(UUID.randomUUID());
    }

    return results;
  }

  public record MetricFilter(
      String serviceType,
      String resourceId,
      String resourceType,
      String filter,
      Metric metrics,
      String region) {}
}
