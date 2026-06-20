package com.cloudsherpa.ingestion.provider.gcp.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.gcp.GcpClientFactory;
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
import org.springframework.stereotype.Component;

@Component
public class GcpCloudMonitoringMetricProvider implements CloudMonitoringMetricProvider {
  private MetricServiceClient buildClient(CloudCredentials credentials) throws IOException {

    GoogleCredentials googleCredentials = GcpClientFactory.credentials(credentials);

    MetricServiceSettings settings =
        MetricServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
            .build();

    return MetricServiceClient.create(settings);
  }

  private MetricFilter buildFilter(
      String resourceLabel, String resourceType, Metric metric, String resourceId) {

    StringBuilder filter = new StringBuilder();

    filter.append("resource.type=\"").append(resourceType).append("\" ");

    filter
        .append("AND resource.labels.\"")
        .append(resourceLabel)
        .append("\"=\"")
        .append(resourceId)
        .append("\" ");

    filter.append("AND metric.type=\"").append(metric.getName()).append("\"");

    return new MetricFilter(filter.toString(), metric);
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
    for (String instanceValue : instance.getValues()) {
      for (Metric metric : scope.getMetrics()) {
        MetricFilter filter =
            buildFilter(instance.getIdentifierName(), scope.getName(), metric, instanceValue);
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

  private List<UsageRecordModel> processSeries(TimeSeries series, Metric metric) {
    List<UsageRecordModel> results = new ArrayList<>();

    for (Point point : series.getPointsList()) {

      UsageRecordModel usage = new UsageRecordModel();

      usage.setMetricName(metric.getName());

      usage.setUnit(metric.getUnit());

      usage.setTimestamp(Instant.ofEpochSecond(point.getInterval().getEndTime().getSeconds()));

      usage.setValue(extractValue(point));

      results.add(usage);
    }

    return results;
  }

  @Override
  public List<UsageRecordModel> collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request) {
    MetricServiceClient client = null;
    try {
      client = buildClient(request.getCredentials());
    } catch (IOException e) {
      e.printStackTrace();
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

      client
          .listTimeSeries(metricRequest)
          .iterateAll()
          .forEach(series -> results.addAll(processSeries(series, metricFilter.metrics())));
    }

    return results;
  }

  public record MetricFilter(String filter, Metric metrics) {}
}
