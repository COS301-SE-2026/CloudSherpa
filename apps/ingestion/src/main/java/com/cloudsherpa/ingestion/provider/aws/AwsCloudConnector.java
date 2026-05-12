package com.cloudsherpa.ingestion.provider.aws;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.*;

import java.time.Instant;
import java.util.*;
import java.util.List;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Reservation;

@Component("AWS")
public class AwsCloudConnector implements CloudConnector, UsageCapable, BillingCapable {

  private final CloudWatchClient client = CloudWatchClient.builder()
      .credentialsProvider(DefaultCredentialsProvider.create())
      .region(Region.AF_SOUTH_1)
      .build();

  public List<String> getAllEC2InstanceIds(Ec2Client ec2) {
    List<String> instanceIds = new ArrayList<>();

    DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();

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
  public List<UsageRecordModel> fetchUsage(IngestionRequestEvent request) {
    DefaultCredentialsProvider.create();
    Ec2Client ec2 = Ec2Client.builder()
        .region(Region.AF_SOUTH_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    List<String> instanceIds = getAllEC2InstanceIds(ec2);

    List<UsageRecordModel> result = new ArrayList<>();
    for (AccountScope accScope : request.getScopes()) { // one user may have multiple aws accounts, these can be
                                                        // monitored with one request
      for (ServiceScope serviceScope : accScope.getServiceScopes()) { // these are for services such as EC2, RDS etc.

        for (String instanceId : instanceIds) { // instances within a service e.g. i-23xxxxxxx
          Dimension dimension = Dimension.builder().name("InstanceId").value(instanceId).build();

          for (String metric : serviceScope.getMetrics()) { // the metrics requested, e.g. CPUUtilisation, NetworkIn,
                                                            // NetworkOut etc.
            GetMetricStatisticsRequest req = GetMetricStatisticsRequest.builder()
                .namespace(serviceScope.getName())
                .metricName(metric)
                .startTime(request.getFrom())
                .endTime(request.getTo())
                .period(600)
                .dimensions(dimension)
                .statistics(Statistic.AVERAGE)
                .build();

            for (Datapoint dp : client.getMetricStatistics(req).datapoints()) {

              UsageRecordModel r = new UsageRecordModel();
              r.setProvider(accScope.getProvider());
              r.setAccountId(accScope.getAccountId());
              r.setServiceName(serviceScope.getName());
              r.setMetricName(metric);
              r.setValue(dp.average());
              r.setUnit(dp.unit().name());
              r.setTimestamp(dp.timestamp());
              r.setIngestionTimestamp(Instant.now());
              r.setRecordId(UUID.randomUUID());

              result.add(r);
            }
          }
        }
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
