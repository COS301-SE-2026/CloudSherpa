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
    UUID ingestionID = UUID.randomUUID();
    int period = request.getPeriod();
    List<TimeWindow> timeWindows =
        createTimeWindows(request.getFrom(), request.getTo(), period); // create time windows
    // of under 1440
    // datapoints, AWS API
    // limit
    CloudWatchClient client = defaultClient;

    List<UsageRecordModel> result = new ArrayList<>();
    for (ServiceScope serviceScope :
        accountScope.getServiceScopes()) { // for services such as EC2, RDS etc.

      for (InstanceScope instance :
          serviceScope.getInstances()) { // instances within a service with a name and
        // value list e.g. i-23xxxxxxx
        for (Instance instanceValue : instance.getInstances()) { // the specific instance
          Dimension dimension =
              Dimension.builder()
                  .name(instance.getIdentifierName())
                  .value(instanceValue.getIdentifier())
                  .build();
          if (request.getCredentials() != null) {
            client = createClient(request, instanceValue.getRegion());
          }

          for (Metric metric :
              serviceScope.getMetrics()) { // the metrics requested, e.g. CPUUtilisation,
            // NetworkIn, NetworkOut etc.

            AwsMetricRequestContext context =
                new AwsMetricRequestContext(
                    accountScope,
                    serviceScope,
                    instance,
                    instanceValue.getIdentifier(),
                    metric.getName(),
                    period,
                    ingestionID);

            for (TimeWindow window :
                timeWindows) { // ensures requests to AWS are under 1440 datapoint limit
              GetMetricStatisticsRequest req =
                  GetMetricStatisticsRequest.builder()
                      .namespace(serviceScope.getName())
                      .metricName(metric.getName())
                      .startTime(window.from())
                      .endTime(window.to())
                      .period(period)
                      .dimensions(dimension)
                      .statistics(Statistic.AVERAGE)
                      .build();

              result.addAll(buildRequestResult(client, req, context));
            }
          }
        }
      }
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
      UUID ingestionId) {}

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
