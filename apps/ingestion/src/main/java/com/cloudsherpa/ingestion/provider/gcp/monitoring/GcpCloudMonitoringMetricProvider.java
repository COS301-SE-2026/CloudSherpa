package com.cloudsherpa.ingestion.provider.gcp.monitoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

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

@Component
public class GcpCloudMonitoringMetricProvider implements CloudMonitoringMetricProvider {
  private MetricServiceClient buildClient(CloudCredentials credentials) throws IOException {

    GoogleCredentials googleCredentials = GcpClientFactory.credentials(credentials);

    MetricServiceSettings settings = MetricServiceSettings.newBuilder()
        .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
        .build();

    return MetricServiceClient.create(settings);
  }

  private String buildFilter(String resourceType,
      String serviceType, List<Metric> metricType, String resourceId) {
    if (metricType.isEmpty()) {
      throw new IllegalArgumentException("Metric list for a resource may not be empty");
    }
    String filter = String.format(
        "resource.type=\"%s\" "
            + "AND resource.labels.\"%s\"=\"%s\" AND (",
        serviceType,
        resourceType,
        resourceId);
    filter.concat(metricType.removeFirst().getName());
    for (Metric metric : metricType) {
      filter.concat(String.format(" OR metric.type=\"%s\" ", metric.getName()));
    }

    return filter;
  }

  private List<String> processServiceScope(ServiceScope scope) {
    List<String> filters = new ArrayList<>();
    for (InstanceScope instance : scope.getInstances()) {
      filters.addAll(processInstanceScope(scope, instance));
    }
    return filters;
  }

  private List<String> processInstanceScope(ServiceScope scope, InstanceScope instance) {
    List<String> filters = new ArrayList<>();
    for (String instanceValue : instance.getValues()) {
      String filter = buildFilter(instance.getIdentifierName(), scope.getName(), scope.getMetrics(), instanceValue);
      filters.add(filter);
    }
    return filters;
  }

  private List<UsageRecordModel> processSeries(
      TimeSeries series) {
    series.getPoints()
      throw new UnsupportedOperationException("");
  }

  @Override
  public List<UsageRecordModel> collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request) {
    MetricServiceClient client = buildClient(request.getCredentials());
    String projectName = request.getCredentials().getProjectId();

    Aggregation aggregation = Aggregation.newBuilder()
        .setAlignmentPeriod(
            Duration.newBuilder()
                .setSeconds(request.getPeriod())
                .build())
        .setPerSeriesAligner(
            Aggregation.Aligner.ALIGN_MEAN)
        .build();

    TimeInterval interval = TimeInterval.newBuilder()
        .setStartTime(
            Timestamps.fromMillis(
                request.getFrom().toEpochMilli()))
        .setEndTime(
            Timestamps.fromMillis(
                request.getTo().toEpochMilli()))
        .build();
    List<String> requestFilters = new ArrayList<>();
    for (ServiceScope scope : accountScope.getServiceScopes()) { // we build filters per instanceId and return all of
                                                                 // them
      requestFilters.addAll(processServiceScope(scope));
    }

    List<UsageRecordModel> results = new ArrayList<>();

    for (String filter : requestFilters) {
      ListTimeSeriesRequest metricRequest = ListTimeSeriesRequest.newBuilder()
          .setName(projectName)
          .setFilter(filter)
          .setInterval(interval)
          .setAggregation(aggregation)
          .setView(
              ListTimeSeriesRequest.TimeSeriesView.FULL)
          .build();
      client.listTimeSeries(metricRequest)
          .iterateAll()
          .forEach(series -> processSeries(
              resource,
              metric,
              series,
              results));
    }

    return results;

    throw new UnsupportedOperationException("Unimplemented method 'collectMetrics'");
  }
}
