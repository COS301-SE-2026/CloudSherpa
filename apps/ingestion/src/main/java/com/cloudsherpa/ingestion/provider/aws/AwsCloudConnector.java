package com.cloudsherpa.ingestion.provider.aws;

import org.springframework.stereotype.Component;

import java.util.List;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.*;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.util.*;

@Component("AWS")
public class AwsCloudConnector implements CloudConnector, UsageCapable, BillingCapable {

  private final CloudWatchClient client = CloudWatchClient.builder()
      .region(Region.AF_SOUTH_1)
      .build();

  @Override
  public List<UsageRecordModel> fetchUsage(AccountScope scope, IngestionRequestEvent request) {

    GetMetricStatisticsRequest req = GetMetricStatisticsRequest.builder()
        .namespace("AWS/EC2")
        .metricName("CPUUtilization")
        .startTime(request.getFrom())
        .endTime(request.getTo())
        .period(300)
        .statistics(Statistic.AVERAGE)
        .build();

    List<UsageRecordModel> result = new ArrayList<>();

    for (Datapoint dp : client.getMetricStatistics(req).datapoints()) {

      UsageRecordModel r = new UsageRecordModel();
      r.setProvider("AWS");
      r.setAccountId(scope.getAccountId());
      r.setServiceName("EC2");
      r.setMetricName("CPUUtilization");
      r.setValue(dp.average());
      r.setTimestamp(dp.timestamp());

      result.add(r);
    }

    return result;
  }

  @Override
  public List<BillingRecordModel> fetchBilling(AccountScope scope, IngestionRequestEvent request) {
    return List.of(); // mock for now
  }

  @Override
  public boolean testConnection(CloudCredentials credentials) {
    try {
      client.listMetrics();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override

  public String getProviderName() {
    return "AWS";
  }
}
