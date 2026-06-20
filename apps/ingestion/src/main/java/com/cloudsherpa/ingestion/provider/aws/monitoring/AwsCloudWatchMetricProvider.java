package com.cloudsherpa.ingestion.provider.aws.monitoring;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import java.time.Duration;
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
public class AwsCloudWatchMetricProvider implements CloudWatchMetricProvider {
  private final CloudWatchClient defaultClient =
      CloudWatchClient.builder()
          .credentialsProvider(DefaultCredentialsProvider.create())
          .region(Region.EU_NORTH_1)
          .build();

  public List<UsageRecordModel> collectMetrics(
      AccountScope accountScope, IngestionRequestEvent request) {
    UUID ingestionID = UUID.randomUUID();
    int period =
        request
            .getPeriod(); // contract: ensure that the request does not return over 1000 datapoints
    // ((to-from)/period)
    if (period <= 0) {
      throw new IllegalArgumentException("Period must be > 0");
    }
    if ((Duration.between(request.getFrom(), request.getTo()).getSeconds()) / period > 1440) {
      throw new IllegalArgumentException("AWS will not return over 1440 datapoints per metric");
    }
    CloudWatchClient client = defaultClient;
    if (request.getCredentials() != null) {
      AwsBasicCredentials credentials =
          AwsBasicCredentials.create(
              request.getCredentials().getAccessKey(), request.getCredentials().getSecretKey());
      client =
          CloudWatchClient.builder()
              .credentialsProvider(StaticCredentialsProvider.create(credentials))
              .region(Region.of(request.getCredentials().getAwsRegion()))
              .build();
    }

    List<UsageRecordModel> result = new ArrayList<>();
    for (ServiceScope serviceScope :
        accountScope.getServiceScopes()) { // these are for services such as EC2, RDS
      // etc.

      for (InstanceScope instance :
          serviceScope.getInstances()) { // instances within a service with a name and
        // value
        // list e.g. i-23xxxxxxx
        for (String instanceValue : instance.getValues()) { // the specific instance
          Dimension dimension =
              Dimension.builder().name(instance.getIdentifierName()).value(instanceValue).build();

          for (Metric metric :
              serviceScope.getMetrics()) { // the metrics requested, e.g. CPUUtilisation,
            // NetworkIn,
            // NetworkOut etc.
            GetMetricStatisticsRequest req =
                GetMetricStatisticsRequest.builder()
                    .namespace(serviceScope.getName())
                    .metricName(metric.getName())
                    .startTime(request.getFrom())
                    .endTime(request.getTo())
                    .period(period)
                    .dimensions(dimension)
                    .statistics(Statistic.AVERAGE)
                    .build();

            AwsMetricRequestContext context =
                new AwsMetricRequestContext(
                    accountScope,
                    serviceScope,
                    instance,
                    instanceValue,
                    metric.getName(),
                    period,
                    ingestionID);

            result.addAll(buildRequestResult(client, req, context));
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
      r.setTimestamp(dp.timestamp());
      r.setIngestionTimestamp(Instant.now());
      r.setRecordId(UUID.randomUUID());
      r.setResourceId(context.instanceValue());
      r.setResourceType(context.instanceScope().getIdentifierName());
      r.setRegion(Region.AF_SOUTH_1.toString());
      r.setIngestionId(context.ingestionId().toString());
      r.setDimensions(Map.of("Dimensions", req.dimensions().toString()));
      r.setSource("CloudWatch");
      r.setPeriodStart(dp.timestamp().minusSeconds(context.period()));
      r.setPeriodEnd(dp.timestamp());

      records.add(r);
    }

    return records;
  }
}
