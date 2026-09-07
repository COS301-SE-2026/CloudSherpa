package com.cloudsherpa.ingestion.provider.aws.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.Instance;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.monitoring.CloudMonitoringMetricProvider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

@Component
public class AwsCloudWatchMetricProvider implements CloudMonitoringMetricProvider {
  private record TimeWindow(Instant from, Instant to) {}

  private final CloudWatchClient defaultClient =
      CloudWatchClient.builder()
          .credentialsProvider(DefaultCredentialsProvider.create())
          .region(Region.EU_NORTH_1)
          .build();

  public List<UsageRecordModel> collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request) {

    UUID ingestionId = UUID.randomUUID();
    int period = request.getPeriod();
    // Split timewindows into acceptable AWS request limit for API: 1440 datapoints
    // per request
    List<TimeWindow> timeWindows = createTimeWindows(request.getFrom(), request.getTo(), period);

    List<UsageRecordModel> result = new ArrayList<>();

    for (ServiceScope serviceScope : accountScope.getServiceScopes()) { // EC2, RDS etc.
      result.addAll(
          collectServiceMetrics(
              accountScope, request, serviceScope, timeWindows, ingestionId, period));
    }

    return result;
  }

  private List<UsageRecordModel> collectServiceMetrics(
      AccountScope accountScope,
      IngestionRequestEvent request,
      ServiceScope serviceScope,
      List<TimeWindow> timeWindows,
      UUID ingestionId,
      int period) {

    List<UsageRecordModel> result = new ArrayList<>();

    for (InstanceScope instance : serviceScope.getInstances()) {
      result.addAll(
          collectInstanceScopeMetrics(
              accountScope, request, serviceScope, instance, timeWindows, ingestionId, period));
    }

    return result;
  }

  private List<UsageRecordModel> collectInstanceScopeMetrics(
      AccountScope accountScope,
      IngestionRequestEvent request,
      ServiceScope serviceScope,
      InstanceScope instance,
      List<TimeWindow> timeWindows,
      UUID ingestionId,
      int period) {

    List<UsageRecordModel> result = new ArrayList<>();

    for (Instance instanceValue : instance.getInstances()) {
      CloudWatchClient instanceClient = createClient(request, instanceValue.getRegion());

      Dimension dimension =
          Dimension.builder()
              .name(instance.getIdentifierName())
              .value(instanceValue.getIdentifier())
              .build();

      AwsInstanceContext context =
          new AwsInstanceContext(
              accountScope,
              serviceScope,
              instance,
              instanceValue,
              dimension,
              timeWindows,
              instanceClient,
              ingestionId,
              period);

      result.addAll(collectInstanceMetrics(context));
    }

    return result;
  }

  private List<UsageRecordModel> collectInstanceMetrics(AwsInstanceContext context) {

    List<UsageRecordModel> result = new ArrayList<>();

    for (Metric metric : context.serviceScope().getMetrics()) { // metrics such as CPU Utilization

      AwsMetricRequestContext metricContext =
          new AwsMetricRequestContext(
              context.accountScope(),
              context.serviceScope(),
              context.instanceScope(),
              context.instance().getIdentifier(),
              metric.getName(),
              context.period(),
              context.ingestionId(),
              context.dimension(),
              context.timeWindows(),
              context.client());

      result.addAll(collectMetric(metricContext));
    }

    return result;
  }

  private List<UsageRecordModel> collectMetric(AwsMetricRequestContext context) {

    List<UsageRecordModel> result = new ArrayList<>();

    for (TimeWindow window : context.timeWindows()) { // AWS batching windows of 1440 datapoints

      GetMetricStatisticsRequest request =
          GetMetricStatisticsRequest.builder()
              .namespace(context.serviceScope().getName())
              .metricName(context.metric())
              .startTime(window.from())
              .endTime(window.to())
              .period(context.period())
              .dimensions(context.dimension())
              .statistics(Statistic.AVERAGE)
              .build();

      result.addAll(buildRequestResult(context.client(), request, context));
    }

    return result;
  }

  private record AwsMetricRequestContext(
      AccountScope accountScope,
      ServiceScope serviceScope,
      InstanceScope instanceScope,
      String instanceValue,
      String metric,
      int period,
      UUID ingestionId,
      Dimension dimension,
      List<TimeWindow> timeWindows,
      CloudWatchClient client) {}

  private record AwsInstanceContext(
      AccountScope accountScope,
      ServiceScope serviceScope,
      InstanceScope instanceScope,
      Instance instance,
      Dimension dimension,
      List<TimeWindow> timeWindows,
      CloudWatchClient client,
      UUID ingestionId,
      int period) {}

  private List<UsageRecordModel> buildRequestResult(
      CloudWatchClient client, GetMetricStatisticsRequest req, AwsMetricRequestContext context) {

    List<UsageRecordModel> records = new ArrayList<>();

    for (Datapoint dp : client.getMetricStatistics(req).datapoints()) {

      UsageRecordModel r = new UsageRecordModel();

      r.setProvider(context.accountScope().getProvider());
      r.setAccountId(context.accountScope().getAccountId());
      r.setServiceName(context.serviceScope().getName());
      r.setMetricName(context.metric());
      r.setValue(dp.average());
      r.setUnit(dp.unit().name());
      r.setRegion(client.serviceClientConfiguration().region().id());
      r.setTimestamp(dp.timestamp());
      r.setIngestionTimestamp(Instant.now());
      r.setRecordId(UUID.randomUUID());
      r.setResourceId(context.instanceValue());
      r.setResourceType(context.instanceScope().getIdentifierName());
      r.setIngestionId(context.ingestionId().toString());
      r.setDimensions(Map.of("Dimensions", req.dimensions().toString()));
      r.setSource("CloudWatch");
      r.setPeriodStart(dp.timestamp().minusSeconds(context.period()));
      r.setPeriodEnd(dp.timestamp());

      records.add(r);
    }

    return records;
  }

  private CloudWatchClient createClient(IngestionRequestEvent request, String region) {

    if (request.getCredentials() == null) {
      return defaultClient;
    }

    AwsBasicCredentials credentials =
        AwsBasicCredentials.create(
            request.getCredentials().getAccessKeyId(),
            request.getCredentials().getSecretAccessKey());

    return CloudWatchClient.builder()
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .region(Region.of(region))
        .build();
  }

  private List<TimeWindow> createTimeWindows(Instant from, Instant to, int period) {

    if (period <= 0) {
      throw new IllegalArgumentException("Period must be > 0");
    }

    if (from == null || to == null) {
      throw new IllegalArgumentException("From and to must not be null");
    }

    if (!to.isAfter(from)) {
      throw new IllegalArgumentException("To must be after from");
    }

    long maxWindowSeconds = (long) period * 1440;

    List<TimeWindow> windows = new ArrayList<>();

    Instant windowStart = from;

    while (windowStart.isBefore(to)) {
      Instant windowEnd = windowStart.plusSeconds(maxWindowSeconds);

      if (windowEnd.isAfter(to)) {
        windowEnd = to;
      }

      windows.add(new TimeWindow(windowStart, windowEnd));
      windowStart = windowEnd;
    }

    return windows;
  }
}
