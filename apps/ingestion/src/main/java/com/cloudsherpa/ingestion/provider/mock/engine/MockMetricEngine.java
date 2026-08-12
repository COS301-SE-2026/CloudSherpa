package com.cloudsherpa.ingestion.provider.mock.engine;

import com.cloudsherpa.ingestion.connector.AccountScope;
import com.cloudsherpa.ingestion.connector.Instance;
import com.cloudsherpa.ingestion.connector.InstanceScope;
import com.cloudsherpa.ingestion.connector.Metric;
import com.cloudsherpa.ingestion.connector.ServiceScope;
import com.cloudsherpa.ingestion.models.IngestionRequestEvent;
import com.cloudsherpa.ingestion.models.UsageRecordModel;
import com.cloudsherpa.ingestion.provider.mock.definition.MetricDefinition;
import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import com.cloudsherpa.ingestion.provider.mock.registry.MockMetricRegistry;
import com.cloudsherpa.ingestion.provider.mock.simulation.MetricSimulationContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.UUID;

public class MockMetricEngine {

  private static final int MAX_DATA_POINTS = 1440;
  private static final int SECONDS_PER_HOUR = 3600;
  private static final int SECONDS_PER_DAY = 86400;
  private static final double INITIAL_CLUSTER_STATE = 50.0;
  private static final double INITIAL_CLUSTER_VARIANCE = 10.0;
  private static final double CLUSTER_NOISE = 1.5;
  private static final double CLUSTER_BURST_FACTOR = 0.05;
  private static final double MIN_THETA = 0.05;
  private static final double THETA_VARIANCE = 0.1;
  private static final double MIN_VOLATILITY = 1.0;
  private static final double VOLATILITY_VARIANCE = 5.0;
  private static final double METRIC_MEAN_VARIANCE = 5.0;
  private static final double DAILY_SEASONAL_WEIGHT = 8.0;
  private static final double WEEKLY_SEASONAL_WEIGHT = 3.0;
  private static final double MAINTENANCE_PENALTY = -10.0;

  private final MockMetricRegistry registry;

  public MockMetricEngine(MockMetricRegistry registry) {
    this.registry = Objects.requireNonNull(registry);
  }

  /**
   * Generates mock usage usageRecords using the supplied provider registry.
   *
   * <p>The engine contains only provider-agnostic simulation logic. Provider-specific service
   * definitions, metric definitions, units, profiles and simulators are supplied by the registry.
   */
  public List<UsageRecordModel> collectMetrics(IngestionRequestEvent request) {
    validateRequest(request);

    UUID ingestionId = UUID.randomUUID();
    long globalSeed =
        Objects.hash(request.getFrom().toEpochMilli(), request.getTo().toEpochMilli());

    List<UsageRecordModel> result = new ArrayList<>();

    for (AccountScope currentAccountScope : request.getScopes()) {
      UsageRequestContext requestContext =
          new UsageRequestContext(
              currentAccountScope,
              request,
              ingestionId,
              Objects.hash(globalSeed, currentAccountScope.getAccountId()));

      result.addAll(processAccountScope(requestContext));
    }
    return result;
  }

  private List<UsageRecordModel> processAccountScope(UsageRequestContext requestContext) {
    List<UsageRecordModel> result = new ArrayList<>();

    for (ServiceScope serviceScope : requestContext.accountScope().getServiceScopes()) {
      result.addAll(processServiceScope(requestContext, serviceScope));
    }
    return result;
  }

  private List<UsageRecordModel> processServiceScope(
      UsageRequestContext requestContext, ServiceScope serviceScope) {
    MockServiceDefinition serviceDefinition = registry.service(serviceScope.getName());

    MutableDouble clusterState =
        new MutableDouble(
            INITIAL_CLUSTER_STATE
                + new Random(requestContext.accountSeed()).nextGaussian()
                    * INITIAL_CLUSTER_VARIANCE);

    ServiceSimulationContext simulationContext =
        new ServiceSimulationContext(serviceScope, serviceDefinition, clusterState);
    List<UsageRecordModel> result = new ArrayList<>();

    for (InstanceScope instanceScope : serviceScope.getInstances()) {
      result.addAll(processInstanceScope(requestContext, simulationContext, instanceScope));
    }
    return result;
  }

  private List<UsageRecordModel> processInstanceScope(
      UsageRequestContext requestContext,
      ServiceSimulationContext simulationContext,
      InstanceScope instanceScope) {
    List<UsageRecordModel> result = new ArrayList<>();

    for (Instance instance : instanceScope.getInstances()) {
      String instanceId = instance.getIdentifier();
      result.addAll(processInstance(requestContext, simulationContext, instanceScope, instanceId));
    }
    return result;
  }

  private List<UsageRecordModel> processInstance(
      UsageRequestContext requestContext,
      ServiceSimulationContext simulationContext,
      InstanceScope instanceScope,
      String instanceId) {

    long resourceSeed =
        Objects.hash(
            requestContext.accountSeed(), simulationContext.serviceScope().getName(), instanceId);

    SplittableRandom rng = new SplittableRandom(resourceSeed);

    MetricSimulationState simulationState =
        createSimulationState(
            simulationContext.serviceDefinition(), simulationContext.serviceScope(), rng);

    List<UsageRecordModel> result = new ArrayList<>();

    int count = 0;

    for (Instant timestamp = requestContext.request().getFrom();
        !timestamp.isAfter(requestContext.request().getTo());
        timestamp = timestamp.plusSeconds(requestContext.request().getPeriod())) {

      if (++count > MAX_DATA_POINTS) {
        break;
      }

      result.addAll(
          processTimestamp(
              requestContext,
              simulationContext,
              instanceScope,
              instanceId,
              simulationState,
              rng,
              timestamp));
    }
    return result;
  }

  private List<UsageRecordModel> processTimestamp(
      UsageRequestContext requestContext,
      ServiceSimulationContext simulationContext,
      InstanceScope instanceScope,
      String instanceId,
      MetricSimulationState simulationState,
      SplittableRandom rng,
      Instant timestamp) {

    SeasonalFactors seasonalFactors =
        calculateSeasonalFactors(timestamp, rng, simulationContext.serviceDefinition());
    double clusterFactor =
        updateClusterFactor(rng, seasonalFactors.burst(), simulationContext.clusterState());

    List<UsageRecordModel> result = new ArrayList<>();

    for (Metric metric : simulationContext.serviceScope().getMetrics()) {
      MetricDefinition metricDefinition =
          simulationContext.serviceDefinition().metric(metric.getName());
      double value =
          simulateMetric(metricDefinition, simulationState, rng, seasonalFactors, clusterFactor);

      result.add(
          buildUsageRecord(
              requestContext,
              simulationContext,
              instanceScope,
              instanceId,
              metricDefinition,
              value,
              timestamp));
    }
    return result;
  }

  private double simulateMetric(
      MetricDefinition metricDefinition,
      MetricSimulationState simulationState,
      SplittableRandom rng,
      SeasonalFactors seasonalFactors,
      double clusterFactor) {

    String metricName = metricDefinition.name();
    double state = simulationState.metricState().get(metricName);
    double mean = simulationState.metricMean().get(metricName);
    double gaussian = rng.nextGaussian();
    double drift = simulationState.theta() * (mean - state);
    double noise = gaussian * simulationState.volatility();

    state =
        state + drift + noise + seasonalFactors.seasonal() + seasonalFactors.maintenancePenalty();
    state += seasonalFactors.burst() * metricDefinition.profile().burstWeight();

    simulationState.metricState().put(metricName, state);

    MetricSimulationContext context =
        new MetricSimulationContext(
            state, gaussian, clusterFactor, seasonalFactors.burst(), metricDefinition.profile());

    return metricDefinition.simulator().simulate(context);
  }

  private MetricSimulationState createSimulationState(
      MockServiceDefinition serviceDefinition, ServiceScope serviceScope, SplittableRandom rng) {
    double mean = serviceDefinition.baseLoad() + rng.nextDouble() * serviceDefinition.variance();
    double theta = MIN_THETA + rng.nextDouble() * THETA_VARIANCE;
    double volatility = MIN_VOLATILITY + rng.nextDouble() * VOLATILITY_VARIANCE;
    Map<String, Double> metricState = new HashMap<>();
    Map<String, Double> metricMean = new HashMap<>();

    for (Metric metric : serviceScope.getMetrics()) {
      metricState.put(metric.getName(), mean);
      metricMean.put(metric.getName(), mean + rng.nextGaussian() * METRIC_MEAN_VARIANCE);
    }
    return new MetricSimulationState(metricState, metricMean, theta, volatility);
  }

  private SeasonalFactors calculateSeasonalFactors(
      Instant timestamp, SplittableRandom rng, MockServiceDefinition serviceDefinition) {
    double seconds = timestamp.getEpochSecond();
    double daily = Math.sin(seconds / SECONDS_PER_DAY * 2 * Math.PI);
    double weekly = Math.sin(seconds / (SECONDS_PER_DAY * 7.0) * 2 * Math.PI);
    double seasonal = DAILY_SEASONAL_WEIGHT * daily + WEEKLY_SEASONAL_WEIGHT * weekly;
    boolean maintenance = (seconds % SECONDS_PER_DAY) < 2 * SECONDS_PER_HOUR;
    double maintenancePenalty = maintenance ? MAINTENANCE_PENALTY : 0.0;
    boolean burstEvent = rng.nextDouble() < serviceDefinition.burstChance();
    double burst = burstEvent ? rng.nextDouble() * 50.0 : 0.0;

    return new SeasonalFactors(seasonal, maintenancePenalty, burst);
  }

  private double updateClusterFactor(
      SplittableRandom rng, double burst, MutableDouble clusterState) {
    clusterState.value += rng.nextGaussian() * CLUSTER_NOISE + burst * CLUSTER_BURST_FACTOR;
    return 1.0 + clusterState.value / 100.0;
  }

  private UsageRecordModel buildUsageRecord(
      UsageRequestContext requestContext,
      ServiceSimulationContext simulationContext,
      InstanceScope instanceScope,
      String instanceId,
      MetricDefinition metricDefinition,
      double value,
      Instant timestamp) {

    UsageRecordModel usageRecord = new UsageRecordModel();

    usageRecord.setProvider(requestContext.accountScope().getProvider());
    usageRecord.setAccountId(requestContext.accountScope().getAccountId());
    usageRecord.setProjectId(requestContext.accountScope().getProjectId());
    usageRecord.setRegion("us-central1");
    usageRecord.setServiceName(simulationContext.serviceScope().getName());
    usageRecord.setMetricName(metricDefinition.name());
    usageRecord.setValue(value);
    usageRecord.setUnit(metricDefinition.unit());
    usageRecord.setTimestamp(timestamp);
    usageRecord.setPeriodStart(timestamp.minusSeconds(requestContext.request().getPeriod()));
    usageRecord.setPeriodEnd(timestamp);
    usageRecord.setIngestionTimestamp(Instant.now());
    usageRecord.setRecordId(UUID.randomUUID());
    usageRecord.setResourceId(instanceId);
    usageRecord.setResourceType(instanceScope.getIdentifierName());
    usageRecord.setIngestionId(requestContext.ingestionId().toString());
    usageRecord.setSource("MockMetricProvider");

    return usageRecord;
  }

  private void validateRequest(IngestionRequestEvent request) {
    Objects.requireNonNull(request, "Request cannot be null.");
    Objects.requireNonNull(request.getFrom(), "Request 'from' cannot be null.");
    Objects.requireNonNull(request.getTo(), "Request 'to' cannot be null.");

    if (request.getPeriod() <= 0) {
      throw new IllegalArgumentException("Period must be > 0");
    }

    if (request.getFrom().isAfter(request.getTo())) {
      throw new IllegalArgumentException("Request 'from' must not be after 'to'");
    }
  }

  private record UsageRequestContext(
      AccountScope accountScope,
      IngestionRequestEvent request,
      UUID ingestionId,
      long accountSeed) {}

  private record ServiceSimulationContext(
      ServiceScope serviceScope,
      MockServiceDefinition serviceDefinition,
      MutableDouble clusterState) {}

  private record MetricSimulationState(
      Map<String, Double> metricState,
      Map<String, Double> metricMean,
      double theta,
      double volatility) {}

  private record SeasonalFactors(double seasonal, double maintenancePenalty, double burst) {}

  private static final class MutableDouble {
    private double value;

    private MutableDouble(double value) {
      this.value = value;
    }
  }
}
