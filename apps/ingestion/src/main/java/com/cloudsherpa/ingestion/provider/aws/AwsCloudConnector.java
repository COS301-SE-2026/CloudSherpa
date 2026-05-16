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
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

@Component("AWS")
public class AwsCloudConnector implements CloudConnector, UsageCapable, BillingCapable {

  private final CloudWatchClient client = CloudWatchClient.builder()
      .credentialsProvider(DefaultCredentialsProvider.create())
      .region(Region.AF_SOUTH_1)
      .build();

  public List<String> getAllEC2InstanceIds(CloudCredentials credentials) {

    List<String> instanceIds = new ArrayList<>();

    try (Ec2Client ec2 = Ec2Client.builder()
        .region(AwsClientFactory.region(credentials))
        .credentialsProvider(
            AwsClientFactory.credentialsProvider(credentials))
        .build()) {
      DescribeInstancesResponse response = ec2.describeInstances();

      for (Reservation reservation : response.reservations()) {
        for (Instance instance : reservation.instances()) {
          instanceIds.add(instance.instanceId());
        }
      }
    }
    return instanceIds;
  }

  @Override
  public List<UsageRecordModel> fetchUsage(AccountScope accountScope, IngestionRequestEvent request) {
    UUID ingestionID = UUID.randomUUID();
    int period = request
        .getPeriod(); // contract: ensure that the request does not return over 1000 datapoints
    // ((to-from)/period)
    DefaultCredentialsProvider.create();
    Ec2Client ec2 = Ec2Client.builder()
        .region(Region.AF_SOUTH_1)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

    List<UsageRecordModel> result = new ArrayList<>();
    for (ServiceScope serviceScope : accountScope.getServiceScopes()) { // these are for services such as EC2, RDS etc.

      for (InstanceScope instance : serviceScope.getInstances()) { // instances within a service with a name and value
                                                                   // list e.g. i-23xxxxxxx
        for (String instanceValue : instance.getValues()) { // the specific instance
          Dimension dimension = Dimension.builder().name(instance.getIdentifierName()).value(instanceValue).build();

          for (String metric : serviceScope.getMetrics()) { // the metrics requested, e.g. CPUUtilisation, NetworkIn,
                                                            // NetworkOut etc.
            GetMetricStatisticsRequest req = GetMetricStatisticsRequest.builder()
                .namespace(serviceScope.getName())
                .metricName(metric)
                .startTime(request.getFrom())
                .endTime(request.getTo())
                .period(period)
                .dimensions(dimension)
                .statistics(Statistic.AVERAGE)
                .build();

            for (Datapoint dp : client.getMetricStatistics(req).datapoints()) {

              UsageRecordModel r = new UsageRecordModel();
              r.setProvider(accountScope.getProvider());
              r.setAccountId(accountScope.getAccountId());
              r.setServiceName(serviceScope.getName());
              r.setMetricName(metric);
              r.setValue(dp.average());
              r.setUnit(dp.unit().name());
              r.setTimestamp(dp.timestamp());
              r.setIngestionTimestamp(Instant.now());
              r.setRecordId(UUID.randomUUID());
              r.setResourceId(instanceValue);
              r.setResourceType(instance.getIdentifierName());
              r.setRegion(Region.AF_SOUTH_1.toString());
              r.setIngestionId(ingestionID.toString());
              r.setServiceName(serviceScope.getName());
              r.setSource("CloudWatch");
              r.setPeriodStart(dp.timestamp().minusSeconds(period));
              r.setPeriodEnd(dp.timestamp());

              result.add(r);
            }
          }
        }
      }
    }

    return result;

  }

  @Override
  public List<BillingRecordModel> fetchBilling(AccountScope accountScope, IngestionRequestEvent request) {
    return List.of(); // mock for now
  }

  public List<String> getAllOfferedServices() {
    List<String> services = new ArrayList<>();
    services.add("AWS/EC2");
    services.add("AWS/ECS");
    services.add("AWS/EKS");
    services.add("AWS/Lambda");
    services.add("AWS/RDS");
    services.add("AWS/ElastiCache");
    services.add("AWS/OpenSearch");
    services.add("AWS/MSK");

    return services;
  }

  @Override
  public List<BillingRecordModel> fetchMockBilling(AccountScope accountScope, IngestionRequestEvent request) {
    return List.of();
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

  @Override
  public List<UsageRecordModel> fetchMockUsage(AccountScope accountScope, IngestionRequestEvent request) {

    if (request.getPeriod() <= 0) {
      throw new IllegalArgumentException("Period must be > 0");
    }

    final int maxDatapoints = 1440;
    final int secondsPerHour = 3600;
    final int secondsPerDay = 86400;

    int period = request.getPeriod();
    UUID ingestionID = UUID.randomUUID();
    List<UsageRecordModel> result = new ArrayList<>();

    long globalSeed = Objects.hash(
        request.getFrom().toEpochMilli(),
        request.getTo().toEpochMilli());

    for (AccountScope accScope : request.getScopes()) {

      long accountSeed = Objects.hash(globalSeed, accScope.getAccountId());

      for (ServiceScope serviceScope : accScope.getServiceScopes()) {

        ServiceType type = ServiceType.from(serviceScope.getName());

        double serviceClusterState = 50.0 + new Random(accountSeed).nextGaussian() * 10; // services have partially
                                                                                         // correlated usage data

        for (InstanceScope instance : serviceScope.getInstances()) {

          for (String instanceId : instance.getValues()) {

            long resourceSeed = Objects.hash(accountSeed, serviceScope.getName(), instanceId);
            SplittableRandom rng = new SplittableRandom(resourceSeed);

            double mean = type.baseLoad + rng.nextDouble() * type.variance; // we create different resource
                                                                            // personalities, such that EC2 metrics look
                                                                            // different from RDS metrics for instance

            double theta = 0.05 + rng.nextDouble() * 0.1;
            double volatility = 1.0 + rng.nextDouble() * 5.0;

            Map<String, Double> metricState = new HashMap<>(); // different metrics may have different trajectories and
                                                               // mean loads
            Map<String, Double> metricMean = new HashMap<>();

            for (String metric : serviceScope.getMetrics()) {
              metricState.put(metric, mean);
              metricMean.put(metric, mean + rng.nextGaussian() * 5);
            }

            int count = 0;

            for (Instant t = request.getFrom(); !t.isAfter(request.getTo()); t = t.plusSeconds(request.getPeriod())) {

              if (++count > maxDatapoints)
                break;

              double seconds = t.getEpochSecond();

              // Introducing daily and weekly periodicity to mimic seasonality for data usage
              double daily = Math.sin(seconds / (double) secondsPerDay * 2 * Math.PI);
              double weekly = Math.sin(seconds / (double) (secondsPerDay * 7) * 2 * Math.PI);

              double seasonal = 8 * daily + 3 * weekly;

              boolean maintenance = (seconds % secondsPerDay) < 2 * secondsPerHour;// first 2h of each day simulated as
                                                                                   // maintenance period

              double maintenancePenalty = maintenance ? -10 : 0; // less usage in a maintenance period

              boolean burstEvent = rng.nextDouble() < type.burstChance; // low probability of a burst event with high
                                                                        // usage. Metric dependent chance

              double burst = burstEvent ? rng.nextDouble() * 50 : 0;

              serviceClusterState += rng.nextGaussian() * 1.5 + burst * 0.05; // service level usage correlation (EC2
                                                                              // metrics are related)

              double clusterFactor = 1.0 + (serviceClusterState / 100.0);

              for (String metric : serviceScope.getMetrics()) { // each metric has somewhat different behaviour
                double state = metricState.get(metric);
                double mMean = metricMean.get(metric);

                double gaussian = rng.nextGaussian();

                // Ornstein-Uhlenbeck per metric noise
                double drift = theta * (mMean - state);

                double noise = gaussian * volatility;

                state = state
                    + drift
                    + noise
                    + seasonal
                    + maintenancePenalty;

                // burst affects all metrics to different extents
                state += burst * metricBurstWeight(type, metric);

                metricState.put(metric, state);

                double value = computeMetric(
                    type,
                    metric,
                    state,
                    clusterFactor,
                    gaussian,
                    burst);

                UsageRecordModel r = new UsageRecordModel();

                r.setProvider(accScope.getProvider());
                r.setAccountId(accScope.getAccountId());
                r.setServiceName(serviceScope.getName());
                r.setMetricName(metric);
                r.setValue(value);
                r.setUnit(CloudWatchMetricUnits.unit(type, metric));
                r.setTimestamp(t);
                r.setPeriodStart(r.getTimestamp().minusSeconds(period));
                r.setPeriodEnd(r.getTimestamp());
                r.setIngestionTimestamp(Instant.now());
                r.setRecordId(UUID.randomUUID());
                r.setResourceId(instanceId);
                r.setResourceType(instance.getIdentifierName());
                r.setRegion(Region.AF_SOUTH_1.toString());
                r.setIngestionId(ingestionID.toString());
                r.setSource("MockCloudWatch");

                result.add(r);
              }
            }
          }
        }
      }
    }

    return result;
  }

  enum ServiceType {

    EC2(30, 50, 0.03),
    LAMBDA(5, 80, 0.12),
    RDS(20, 40, 0.02),
    S3(2, 20, 0.01),
    DYNAMODB(10, 60, 0.08),
    ECS_EKS(25, 70, 0.05),
    GPU_ML(15, 90, 0.02);

    final double baseLoad;
    final double variance;
    final double burstChance;

    ServiceType(double baseLoad, double variance, double burstChance) {
      this.baseLoad = baseLoad;
      this.variance = variance;
      this.burstChance = burstChance;
    }

    static ServiceType from(String name) {
      return switch (name.toUpperCase()) {
        case "AWS/EC2" -> EC2;
        case "AWS/LAMBDA" -> LAMBDA;
        case "AWS/RDS" -> RDS;
        case "AWS/S3" -> S3;
        case "AWS/DYNAMODB" -> DYNAMODB;
        case "AWS/ECS", "AWS/EKS" -> ECS_EKS;
        case "AWS/GPU", "AWS/SAGEMAKER" -> GPU_ML;
        default -> EC2;
      };
    }
  }

  private double metricBurstWeight(ServiceType type, String metric) {
    return switch (type) {
      case EC2 -> switch (metric) {
        case "CPUUtilization" -> 0.8;
        case "NetworkIn" -> 1.2;
        case "NetworkOut" -> 1.0;
        default -> 0.5;
      };
      case LAMBDA -> 1.0;
      case RDS -> 0.7;
      case DYNAMODB -> 1.3;
      default -> 0.6;
    };
  }

  private double computeMetric(
      ServiceType type,
      String metric,
      double state,
      double clusterFactor,
      double gaussian,
      double burst) {
    return switch (type) {

      case EC2 -> switch (metric) {
        case "CPUUtilization" -> Math.max(0, Math.min(100, state + gaussian * 2));
        case "NetworkIn" -> Math.max(0, state * 1000 * clusterFactor + burst * 50);
        case "NetworkOut" -> Math.max(0, state * 800 * clusterFactor);
        case "DiskReadOps" -> Math.max(0, Math.abs(state - 50) * 30 + burst);
        case "DiskWriteOps" -> Math.max(0, Math.abs(state - 40) * 25 + burst * 0.5);
        case "DiskReadBytes" -> Math.max(0, state * 1024 * clusterFactor);
        case "DiskWriteBytes" -> Math.max(0, state * 2048 * clusterFactor);
        default -> Math.max(0, state);
      };

      case LAMBDA -> switch (metric) {
        case "Invocations" -> state * 2;
        case "Duration" -> 100 + state * 5 + (burst > 20 ? 1000 : 0);
        case "Errors" -> burst > 30 ? 1 : 0;
        default -> Math.max(0, state);
      };

      case RDS -> switch (metric) {
        case "Latency" -> Math.max(0, 10 + state * 3 + clusterFactor * 20 + burst);
        case "CPUUtilization" -> Math.max(0, Math.min(state, 100));
        case "DatabaseConnections" -> Math.max(0, state * 10);
        default -> Math.max(0, state);
      };

      case S3 -> switch (metric) {
        case "RequestCount" -> state * 20 + burst * 5;
        case "BytesUploaded" -> state * 500;
        default -> Math.max(0, state);
      };

      case DYNAMODB -> switch (metric) {
        case "Throttles" -> burst > 25 ? 1 : 0;
        default -> Math.max(0, state);
      };

      case ECS_EKS -> switch (metric) {
        case "CPUUtilization" -> Math.max(0, Math.min(state, 100));
        case "MemoryUtilization" -> Math.max(0, Math.min(state, 100));
        default -> Math.max(0, state * clusterFactor);
      };

      case GPU_ML -> switch (metric) {
        case "MemoryUtilization" -> Math.max(0, Math.min(state, 100));
        case "GPUUtilization" -> Math.max(0, Math.min(state, 100));
        case "TrainingLoss" -> 1.0 / (1 + state);
        case "BatchTime" -> 100 + (100 - state) * 2;
        default -> Math.max(0, state);
      };
    };
  }

  public final class CloudWatchMetricUnits {

    private CloudWatchMetricUnits() {
    }

    public static final Map<ServiceType, Map<String, String>> UNITS = Map.of(

        ServiceType.EC2, Map.ofEntries(
            Map.entry("CPUUtilization", "Percent"),
            Map.entry("DiskReadOps", "Count"),
            Map.entry("DiskWriteOps", "Count"),
            Map.entry("DiskReadBytes", "Bytes"),
            Map.entry("DiskWriteBytes", "Bytes"),
            Map.entry("NetworkIn", "Bytes"),
            Map.entry("NetworkOut", "Bytes"),
            Map.entry("NetworkPacketsIn", "Count"),
            Map.entry("NetworkPacketsOut", "Count"),
            Map.entry("StatusCheckFailed", "Count"),
            Map.entry("StatusCheckFailed_Instance", "Count"),
            Map.entry("StatusCheckFailed_System", "Count")),

        ServiceType.LAMBDA, Map.ofEntries(
            Map.entry("Invocations", "Count"),
            Map.entry("Errors", "Count"),
            Map.entry("Duration", "Milliseconds"),
            Map.entry("Throttles", "Count"),
            Map.entry("IteratorAge", "Milliseconds"),
            Map.entry("ConcurrentExecutions", "Count"),
            Map.entry("UnreservedConcurrentExecutions", "Count")),

        ServiceType.RDS, Map.ofEntries(
            Map.entry("CPUUtilization", "Percent"),
            Map.entry("DatabaseConnections", "Count"),
            Map.entry("FreeStorageSpace", "Bytes"),
            Map.entry("ReadLatency", "Milliseconds"),
            Map.entry("WriteLatency", "Milliseconds"),
            Map.entry("ReadIOPS", "Count/Second"),
            Map.entry("WriteIOPS", "Count/Second"),
            Map.entry("NetworkReceiveThroughput", "Bytes/Second"),
            Map.entry("NetworkTransmitThroughput", "Bytes/Second"),
            Map.entry("FreeableMemory", "Bytes"),
            Map.entry("SwapUsage", "Bytes")),

        ServiceType.S3, Map.ofEntries(
            Map.entry("NumberOfObjects", "Count"),
            Map.entry("BucketSizeBytes", "Bytes"),
            Map.entry("AllRequests", "Count"),
            Map.entry("GetRequests", "Count"),
            Map.entry("PutRequests", "Count"),
            Map.entry("DeleteRequests", "Count"),
            Map.entry("4xxErrors", "Count"),
            Map.entry("5xxErrors", "Count"),
            Map.entry("FirstByteLatency", "Milliseconds"),
            Map.entry("TotalRequestLatency", "Milliseconds")),

        ServiceType.DYNAMODB, Map.ofEntries(
            Map.entry("ConsumedReadCapacityUnits", "Count"),
            Map.entry("ConsumedWriteCapacityUnits", "Count"),
            Map.entry("ReadThrottleEvents", "Count"),
            Map.entry("WriteThrottleEvents", "Count"),
            Map.entry("ThrottledRequests", "Count"),
            Map.entry("SuccessfulRequestLatency", "Milliseconds"),
            Map.entry("SystemErrors", "Count"),
            Map.entry("UserErrors", "Count")),

        ServiceType.ECS_EKS, Map.ofEntries(
            Map.entry("CPUUtilization", "Percent"),
            Map.entry("MemoryUtilization", "Percent"),
            Map.entry("RunningTaskCount", "Count"),
            Map.entry("PendingTaskCount", "Count"),
            Map.entry("ServiceCount", "Count")),

        ServiceType.GPU_ML, Map.ofEntries(
            Map.entry("GPUUtilization", "Percent"),
            Map.entry("GPUMemoryUtilization", "Percent"),
            Map.entry("CPUUtilization", "Percent"),
            Map.entry("MemoryUtilization", "Percent"),
            Map.entry("DiskUtilization", "Percent"),
            Map.entry("TrainingLoss", "None"),
            Map.entry("BatchSize", "Count"),
            Map.entry("IterationTime", "Milliseconds")));

    public static String unit(ServiceType type, String metric) {
      return UNITS
          .getOrDefault(type, Map.of())
          .getOrDefault(metric, "None");
    }
  }
}
