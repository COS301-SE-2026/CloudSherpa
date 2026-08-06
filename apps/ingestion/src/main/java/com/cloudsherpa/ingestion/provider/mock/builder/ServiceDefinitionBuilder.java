package com.cloudsherpa.ingestion.provider.mock.builder;

import com.cloudsherpa.ingestion.provider.mock.definition.MetricDefinition;
import com.cloudsherpa.ingestion.provider.mock.definition.MockServiceDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Builder used by MockRegistryBuilder to construct immutable MockServiceDefinition instances. */
public class ServiceDefinitionBuilder {

  private final MockRegistryBuilder registryBuilder;

  private final String serviceName;

  private final Map<String, MetricDefinition> metrics = new LinkedHashMap<>();

  private double baseLoad;

  private double variance;

  private double burstChance;

  ServiceDefinitionBuilder(MockRegistryBuilder registryBuilder, String serviceName) {

    this.registryBuilder =
        Objects.requireNonNull(registryBuilder, "Registry builder cannot be null.");

    this.serviceName = Objects.requireNonNull(serviceName, "Service name cannot be null.");
  }

  /** Sets the base load used by the simulation. */
  public ServiceDefinitionBuilder baseLoad(double baseLoad) {

    this.baseLoad = baseLoad;
    return this;
  }

  /** Sets the variance used when generating the initial state. */
  public ServiceDefinitionBuilder variance(double variance) {

    this.variance = variance;
    return this;
  }

  /** Sets the probability of a burst event. */
  public ServiceDefinitionBuilder burstChance(double burstChance) {

    this.burstChance = burstChance;
    return this;
  }

  /** Adds a metric to this service. */
  public ServiceDefinitionBuilder metric(MetricDefinition metric) {

    Objects.requireNonNull(metric, "Metric cannot be null.");

    if (metrics.containsKey(metric.name())) {
      throw new IllegalArgumentException(
          "Metric '"
              + metric.name()
              + "' is already registered for service '"
              + serviceName
              + "'.");
    }

    metrics.put(metric.name(), metric);

    return this;
  }

  /** Convenience method for registering multiple metrics. */
  public ServiceDefinitionBuilder metrics(MetricDefinition... metrics) {

    Objects.requireNonNull(metrics, "Metrics cannot be null.");

    for (MetricDefinition metric : metrics) {
      metric(metric);
    }

    return this;
  }

  /** Builds the immutable service definition. */
  public MockServiceDefinition build() {

    if (metrics.isEmpty()) {
      throw new IllegalStateException("Service '" + serviceName + "' has no metrics.");
    }

    return new MockServiceDefinition(serviceName, baseLoad, variance, burstChance, metrics);
  }

  /**
   * Builds the service and immediately registers it with the registry. Returns the registry builder
   * so another service can be defined.
   */
  public MockRegistryBuilder register() {

    registryBuilder.register(build());

    return registryBuilder;
  }
}
