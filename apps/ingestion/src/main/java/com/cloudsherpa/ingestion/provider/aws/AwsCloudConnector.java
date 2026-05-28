package com.cloudsherpa.ingestion.provider.aws;

import com.cloudsherpa.ingestion.connector.*;
import com.cloudsherpa.ingestion.models.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Reservation;

@Component("aws")
public class AwsCloudConnector implements CloudConnector, UsageCapable, BillingCapable {
  private static final String CPU_UTILIZATION = "CPUUtilization";
  private static final String NETWORK_IN = "NetworkIn";
  private static final String NETWORK_OUT = "NetworkOut";
  private static final String MEMORY_UTILIZATION = "MemoryUtilization";
  private static final String COUNT = "Count";
  private static final String MILLISECONDS = "Milliseconds";
  private static final String bytes = "Bytes";
  private static final String PERCENT = "Percent";

  private CloudWatchClient defaultClient =
      CloudWatchClient.builder()
          .credentialsProvider(DefaultCredentialsProvider.create())
          .region(Region.EU_NORTH_1)
          .build();

  public List<String> getAllEc2InstanceIds(Ec2Client ec2) {
    List<String> instanceIds = new ArrayList<>();

    DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();

    for (DescribeInstancesResponse page : ec2.describeInstancesPaginator(request)) {
      for (Reservation reservation : page.reservations()) {
        for (software.amazon.awssdk.services.ec2.model.Instance instance :
            reservation.instances()) {
          instanceIds.add(instance.instanceId());
        }
      }
    }

    return instanceIds;
  }

  private List<UsageRecordModel> buildRequestResult(
      CloudWatchClient client,
      GetMetricStatisticsRequest req,
      AccountScope accountScope,
      ServiceScope serviceScope,
      InstanceScope instance,
      String instanceValue,
      String metric,
      int period,
      UUID ingestionID) {

    List<UsageRecordModel> records = new ArrayList<>();

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
      r.setSource("CloudWatch");
      r.setPeriodStart(dp.timestamp().minusSeconds(period));
      r.setPeriodEnd(dp.timestamp());

      records.add(r);
    }

    return records;
  }

  @Override
  public List<UsageRecordModel> fetchUsage(
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
              .region(Region.of(request.getCredentials().getRegion()))
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

          for (String metric :
              serviceScope.getMetrics()) { // the metrics requested, e.g. CPUUtilisation,
            // NetworkIn,
            // NetworkOut etc.
            GetMetricStatisticsRequest req =
                GetMetricStatisticsRequest.builder()
                    .namespace(serviceScope.getName())
                    .metricName(metric)
                    .startTime(request.getFrom())
                    .endTime(request.getTo())
                    .period(period)
                    .dimensions(dimension)
                    .statistics(Statistic.AVERAGE)
                    .build();

            result.addAll(
                buildRequestResult(
                    client,
                    req,
                    accountScope,
                    serviceScope,
                    instance,
                    instanceValue,
                    metric,
                    period,
                    ingestionID));
          }
        }
      }
    }

    return result;
  }

  @Override
  public List<BillingRecordModel> fetchBilling(
      AccountScope acCOUNTScope, IngestionRequestEvent request) {
    return List.of(); // mock for now
  }

  @Override
  public List<BillingRecordModel> fetchMockBilling(
      AccountScope acCOUNTScope, IngestionRequestEvent request) {
    return List.of();
  }

  @Override
  public boolean testConnection(CloudCredentials credentials) {
    try {
      defaultClient.listMetrics();
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
  public List<UsageRecordModel> fetchMockUsage(
      AccountScope accountScope, IngestionRequestEvent request) {
    validateRequest(request);
    UUID ingestionID = UUID.randomUUID();
    long globalSeed =
        Objects.hash(request.getFrom().toEpochMilli(), request.getTo().toEpochMilli());
    List<UsageRecordModel> result = new ArrayList<>();
    for (AccountScope accScope : request.getScopes()) {
      result.addAll(processAccountScope(accScope, request, ingestionID, globalSeed));
    }
    return result;
  }

  private void validateRequest(IngestionRequestEvent request) {
    if (request.getPeriod() <= 0) {
      throw new IllegalArgumentException("Period must be > 0");
    }
  }

  private List<UsageRecordModel> processAccountScope(
      AccountScope accScope, IngestionRequestEvent request, UUID ingestionID, long globalSeed) {
    List<UsageRecordModel> result = new ArrayList<>();
    long accountSeed = Objects.hash(globalSeed, accScope.getAccountId());
    for (ServiceScope serviceScope : accScope.getServiceScopes()) {
      result.addAll(processServiceScope(accScope, serviceScope, request, ingestionID, accountSeed));
    }
    return result;
  }

  private List<UsageRecordModel> processServiceScope(
      AccountScope accScope,
      ServiceScope serviceScope,
      IngestionRequestEvent request,
      UUID ingestionID,
      long accountSeed) {
    List<UsageRecordModel> result = new ArrayList<>();
    ServiceType type = ServiceType.from(serviceScope.getName());
    MutableDouble serviceClusterState =
        new MutableDouble(50.0 + new Random(accountSeed).nextGaussian() * 10);
    for (InstanceScope instance : serviceScope.getInstances()) {
      result.addAll(
          processInstanceScope(
              accScope,
              serviceScope,
              instance,
              request,
              ingestionID,
              accountSeed,
              type,
              serviceClusterState));
    }
    return result;
  }

  private List<UsageRecordModel> processInstanceScope(
      AccountScope accScope,
      ServiceScope serviceScope,
      InstanceScope instance,
      IngestionRequestEvent request,
      UUID ingestionID,
      long accountSeed,
      ServiceType type,
      MutableDouble serviceClusterState) {
    List<UsageRecordModel> result = new ArrayList<>();
    for (String instanceId : instance.getValues()) {
      result.addAll(
          processInstance(
              accScope,
              serviceScope,
              instance,
              instanceId,
              request,
              ingestionID,
              accountSeed,
              type,
              serviceClusterState));
    }
    return result;
  }

  private List<UsageRecordModel> processInstance(
      AccountScope accScope,
      ServiceScope serviceScope,
      InstanceScope instance,
      String instanceId,
      IngestionRequestEvent request,
      UUID ingestionID,
      long accountSeed,
      ServiceType type,
      MutableDouble serviceClusterState) {
    List<UsageRecordModel> result = new ArrayList<>();
    long resourceSeed = Objects.hash(accountSeed, serviceScope.getName(), instanceId);
    SplittableRandom rng = new SplittableRandom(resourceSeed);
    MetricSimulationContext context = createSimulationContext(type, rng, serviceScope);
    int COUNT = 0;
    for (Instant t = request.getFrom();
        !t.isAfter(request.getTo());
        t = t.plusSeconds(request.getPeriod())) {
      if (++COUNT > 1440) {
        break;
      }
      result.addAll(
          processTimestamp(
              accScope,
              serviceScope,
              instance,
              instanceId,
              request,
              ingestionID,
              type,
              rng,
              context,
              t,
              serviceClusterState));
    }
    return result;
  }

  private List<UsageRecordModel> processTimestamp(
      AccountScope accScope,
      ServiceScope serviceScope,
      InstanceScope instance,
      String instanceId,
      IngestionRequestEvent request,
      UUID ingestionID,
      ServiceType type,
      SplittableRandom rng,
      MetricSimulationContext context,
      Instant t,
      MutableDouble serviceClusterState) {
    List<UsageRecordModel> result = new ArrayList<>();
    SeasonalFactors seasonalFactors = calculateSeasonalFactors(t, rng, type);
    double clusterFactor =
        updateClusterFactor(rng, seasonalFactors.getBurst(), serviceClusterState);
    for (String metric : serviceScope.getMetrics()) {
      double value = computeMetricValue(metric, type, rng, context, seasonalFactors, clusterFactor);
      result.add(
          buildUsageRecord(
              accScope,
              serviceScope,
              instance,
              instanceId,
              request,
              ingestionID,
              metric,
              value,
              type,
              t));
    }
    return result;
  }

  public class SeasonalFactors {
    private final double seasonal;
    private final double maintenancePenalty;
    private final double burst;

    public SeasonalFactors(double seasonal, double maintenancePenalty, double burst) {
      this.seasonal = seasonal;
      this.maintenancePenalty = maintenancePenalty;
      this.burst = burst;
    }

    public double getSeasonal() {
      return seasonal;
    }

    public double getMaintenancePenalty() {
      return maintenancePenalty;
    }

    public double getBurst() {
      return burst;
    }
  }

  private SeasonalFactors calculateSeasonalFactors(
      Instant t, SplittableRandom rng, ServiceType type) {
    final int secondsPerHour = 3600;
    final int secondsPerDay = 86400;
    double seconds = t.getEpochSecond();
    double daily = Math.sin(seconds / secondsPerDay * 2 * Math.PI);
    double weekly = Math.sin(seconds / (secondsPerDay * 7) * 2 * Math.PI);
    double seasonal = 8 * daily + 3 * weekly;
    boolean maintenance = (seconds % secondsPerDay) < 2 * secondsPerHour;
    double maintenancePenalty = maintenance ? -10 : 0;
    boolean burstEvent = rng.nextDouble() < type.burstChance;
    double burst = burstEvent ? rng.nextDouble() * 50 : 0;
    return new SeasonalFactors(seasonal, maintenancePenalty, burst);
  }

  private UsageRecordModel buildUsageRecord(
      AccountScope accScope,
      ServiceScope serviceScope,
      InstanceScope instance,
      String instanceId,
      IngestionRequestEvent request,
      UUID ingestionID,
      String metric,
      double value,
      ServiceType type,
      Instant t) {
    UsageRecordModel r = new UsageRecordModel();
    r.setProvider(accScope.getProvider());
    r.setAccountId(accScope.getAccountId());
    r.setServiceName(serviceScope.getName());
    r.setMetricName(metric);
    r.setValue(value);
    r.setUnit(CloudWatchMetricUnits.unit(type, metric));
    r.setTimestamp(t);
    r.setPeriodStart(t.minusSeconds(request.getPeriod()));
    r.setPeriodEnd(t);
    r.setIngestionTimestamp(Instant.now());
    r.setRecordId(UUID.randomUUID());
    r.setResourceId(instanceId);
    r.setResourceType(instance.getIdentifierName());
    r.setRegion(Region.AF_SOUTH_1.toString());
    r.setIngestionId(ingestionID.toString());
    r.setSource("MockCloudWatch");
    return r;
  }

  public class MetricSimulationContext {

    private final Map<String, Double> metricState;
    private final Map<String, Double> metricMean;

    private final double theta;
    private final double volatility;

    public MetricSimulationContext(
        Map<String, Double> metricState,
        Map<String, Double> metricMean,
        double theta,
        double volatility) {

      this.metricState = metricState;
      this.metricMean = metricMean;
      this.theta = theta;
      this.volatility = volatility;
    }

    public Map<String, Double> getMetricState() {
      return metricState;
    }

    public Map<String, Double> getMetricMean() {
      return metricMean;
    }

    public double getTheta() {
      return theta;
    }

    public double getVolatility() {
      return volatility;
    }
  }

  private MetricSimulationContext createSimulationContext(
      ServiceType type, SplittableRandom rng, ServiceScope serviceScope) {

    double mean = type.baseLoad + rng.nextDouble() * type.variance;

    double theta = 0.05 + rng.nextDouble() * 0.1;

    double volatility = 1.0 + rng.nextDouble() * 5.0;

    Map<String, Double> metricState = new HashMap<>();
    Map<String, Double> metricMean = new HashMap<>();

    for (String metric : serviceScope.getMetrics()) {

      metricState.put(metric, mean);

      metricMean.put(metric, mean + rng.nextGaussian() * 5);
    }

    return new MetricSimulationContext(metricState, metricMean, theta, volatility);
  }

  private double computeMetricValue(
      String metric,
      ServiceType type,
      SplittableRandom rng,
      MetricSimulationContext context,
      SeasonalFactors seasonalFactors,
      double clusterFactor) {

    double state = context.getMetricState().get(metric);

    double mean = context.getMetricMean().get(metric);

    double gaussian = rng.nextGaussian();

    double drift = context.getTheta() * (mean - state);

    double noise = gaussian * context.getVolatility();

    state =
        state
            + drift
            + noise
            + seasonalFactors.getSeasonal()
            + seasonalFactors.getMaintenancePenalty();

    state += seasonalFactors.getBurst() * metricBurstWeight(type, metric);

    context.getMetricState().put(metric, state);

    return computeMetric(type, metric, state, clusterFactor, gaussian, seasonalFactors.getBurst());
  }

  private double updateClusterFactor(
      SplittableRandom rng, double burst, MutableDouble serviceClusterState) {

    serviceClusterState.value += rng.nextGaussian() * 1.5 + burst * 0.05;

    return 1.0 + (serviceClusterState.value / 100.0);
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
        case CPU_UTILIZATION -> 0.8;
        case NETWORK_IN -> 1.2;
        case NETWORK_OUT -> 1.0;
        default -> 0.5;
      };
      case LAMBDA -> 1.0;
      case RDS -> 0.7;
      case DYNAMODB -> 1.3;
      default -> 0.6;
    };
  }

  private static class MutableDouble {
    double value;

    MutableDouble(double value) {
      this.value = value;
    }
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
        case CPU_UTILIZATION -> Math.clamp(state + gaussian * 2, 0, 100);
        case NETWORK_IN -> Math.max(0, state * 1000 * clusterFactor + burst * 50);
        case NETWORK_OUT -> Math.max(0, state * 800 * clusterFactor);
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
        case CPU_UTILIZATION -> Math.clamp(state, 0, 100);
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
        case CPU_UTILIZATION -> Math.clamp(state, 0, 100);
        case MEMORY_UTILIZATION -> Math.clamp(state, 0, 100);
        default -> Math.max(0, state * clusterFactor);
      };

      case GPU_ML -> switch (metric) {
        case MEMORY_UTILIZATION -> Math.clamp(state, 0, 100);
        case "GPUUtilization" -> Math.clamp(state, 0, 100);
        case "TrainingLoss" -> 1.0 / (1 + state);
        case "BatchTime" -> 100 + (100 - state) * 2;
        default -> Math.max(0, state);
      };
    };
  }

  public final class CloudWatchMetricUnits {

    private CloudWatchMetricUnits() {}

    public static final Map<ServiceType, Map<String, String>> UNITS =
        Map.of(
            ServiceType.EC2,
            Map.ofEntries(
                Map.entry(CPU_UTILIZATION, PERCENT),
                Map.entry("DiskReadOps", COUNT),
                Map.entry("DiskWriteOps", COUNT),
                Map.entry("DiskReadBytes", bytes),
                Map.entry("DiskWriteBytes", bytes),
                Map.entry(NETWORK_IN, bytes),
                Map.entry(NETWORK_OUT, bytes),
                Map.entry("NetworkPacketsIn", COUNT),
                Map.entry("NetworkPacketsOut", COUNT),
                Map.entry("StatusCheckFailed", COUNT),
                Map.entry("StatusCheckFailed_Instance", COUNT),
                Map.entry("StatusCheckFailed_System", COUNT)),
            ServiceType.LAMBDA,
            Map.ofEntries(
                Map.entry("Invocations", COUNT),
                Map.entry("Errors", COUNT),
                Map.entry("Duration", MILLISECONDS),
                Map.entry("Throttles", COUNT),
                Map.entry("IteratorAge", MILLISECONDS),
                Map.entry("ConcurrentExecutions", COUNT),
                Map.entry("UnreservedConcurrentExecutions", COUNT)),
            ServiceType.RDS,
            Map.ofEntries(
                Map.entry(CPU_UTILIZATION, PERCENT),
                Map.entry("DatabaseConnections", COUNT),
                Map.entry("FreeStorageSpace", bytes),
                Map.entry("ReadLatency", MILLISECONDS),
                Map.entry("WriteLatency", MILLISECONDS),
                Map.entry("ReadIOPS", "Count/Second"),
                Map.entry("WriteIOPS", "Count/Second"),
                Map.entry("NetworkReceiveThroughput", "Bytes/Second"),
                Map.entry("NetworkTransmitThroughput", "Bytes/Second"),
                Map.entry("FreeableMemory", bytes),
                Map.entry("SwapUsage", bytes)),
            ServiceType.S3,
            Map.ofEntries(
                Map.entry("NumberOfObjects", COUNT),
                Map.entry("BucketSizeBytes", bytes),
                Map.entry("AllRequests", COUNT),
                Map.entry("GetRequests", COUNT),
                Map.entry("PutRequests", COUNT),
                Map.entry("DeleteRequests", COUNT),
                Map.entry("4xxErrors", COUNT),
                Map.entry("5xxErrors", COUNT),
                Map.entry("FirstByteLatency", MILLISECONDS),
                Map.entry("TotalRequestLatency", MILLISECONDS)),
            ServiceType.DYNAMODB,
            Map.ofEntries(
                Map.entry("ConsumedReadCapacityUnits", COUNT),
                Map.entry("ConsumedWriteCapacityUnits", COUNT),
                Map.entry("ReadThrottleEvents", COUNT),
                Map.entry("WriteThrottleEvents", COUNT),
                Map.entry("ThrottledRequests", COUNT),
                Map.entry("SuccessfulRequestLatency", MILLISECONDS),
                Map.entry("SystemErrors", COUNT),
                Map.entry("UserErrors", COUNT)),
            ServiceType.ECS_EKS,
            Map.ofEntries(
                Map.entry(CPU_UTILIZATION, PERCENT),
                Map.entry(MEMORY_UTILIZATION, PERCENT),
                Map.entry("RunningTaskCount", COUNT),
                Map.entry("PendingTaskCount", COUNT),
                Map.entry("ServiceCount", COUNT)),
            ServiceType.GPU_ML,
            Map.ofEntries(
                Map.entry("GPUUtilization", PERCENT),
                Map.entry("GPUMemoryUtilization", PERCENT),
                Map.entry(CPU_UTILIZATION, PERCENT),
                Map.entry(MEMORY_UTILIZATION, PERCENT),
                Map.entry("DiskUtilization", PERCENT),
                Map.entry("TrainingLoss", "None"),
                Map.entry("BatchSize", COUNT),
                Map.entry("IterationTime", MILLISECONDS)));

    public static String unit(ServiceType type, String metric) {
      return UNITS.getOrDefault(type, Map.of()).getOrDefault(metric, "None");
    }
  }
}
