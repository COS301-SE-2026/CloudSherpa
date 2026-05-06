package com.cloudsherpa.ingestion.provider.aws;

import org.apache.logging.log4j.CloseableThreadContext.Instance;
import org.springframework.stereotype.Component;

import java.util.List;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.*;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.Reservation;

import java.util.*;

@Component("AWS")
public class AwsCloudConnector implements CloudConnector, UsageCapable, BillingCapable {

  private final CloudWatchClient client = CloudWatchClient.builder()
      .region(Region.AF_SOUTH_1)
      .build();

  public List<String> getAllInstanceIds(Ec2Client ec2) {
    List<String> instanceIds = new ArrayList<>();

    DescribeInstancesRequest request = DescribeInstancesRequest.builder()
        .build();

    for (DescribeInstancesResponse page : ec2.describeInstancesPaginator(request)) {
      for (Reservation reservation : page.reservations()) {
        for (software.amazon.awssdk.services.ec2.model.Instance instance : reservation.instances()) {
          instanceIds.add(instance.instanceId());
        }
      }
    }

    return instanceIds;
  }

  @Override
  public List<UsageRecordModel> fetchUsage(AccountScope scope, IngestionRequestEvent request) {

    Ec2Client ec2 = Ec2Client.builder().region(Region.AF_SOUTH_1).build();

    List<String> instanceIds = getAllInstanceIds(ec2);

    List<UsageRecordModel> result = new ArrayList<>();

    for (String instanceId : instanceIds) {

      Dimension dimension = Dimension.builder()
          .name("InstanceId")
          .value(instanceId)
          .build();

      GetMetricStatisticsRequest req = GetMetricStatisticsRequest.builder()
          .namespace("AWS/EC2")
          .metricName("CPUUtilization")
          .dimensions(dimension)
          .startTime(request.getFrom())
          .endTime(request.getTo())
          .period(600)
          .statistics(Statistic.AVERAGE)
          .build();

      GetMetricStatisticsResponse response = client.getMetricStatistics(req);

      System.out.println("Instance IDs: " + instanceIds);
      System.out.println("Datapoints: " + response.datapoints().size());
      for (Datapoint dp : response.datapoints()) {

        UsageRecordModel r = new UsageRecordModel();
        r.setProvider("AWS");
        r.setAccountId(scope.getAccountId());
        r.setServiceName("EC2");
        r.setMetricName("CPUUtilization");
        r.setResourceId(instanceId);
        r.setValue(dp.average());
        r.setTimestamp(dp.timestamp());

        result.add(r);
      }
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
