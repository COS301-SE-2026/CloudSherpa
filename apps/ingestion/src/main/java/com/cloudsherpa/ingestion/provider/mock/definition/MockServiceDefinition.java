package com.cloudsherpa.ingestion.provider.mock.definition;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable definition describing how a cloud service should be simulated.
 *
 * <p>The simulation engine uses this class to determine the statistical characteristics of a
 * service together with the metrics that belong to it.
 */
public final class MockServiceDefinition {
  private final String serviceName;
  private final double baseLoad;
  private final double variance;
  private final double burstChance;
  private final Map<String, MetricDefinition> metrics;

  public MockServiceDefinition(
      String serviceName,
      double baseLoad,
      double variance,
      double burstChance,
      Map<String, MetricDefinition> metrics) {

    this.serviceName = Objects.requireNonNull(serviceName, "Service name cannot be null.");

    this.metrics =
        Collections.unmodifiableMap(
            new LinkedHashMap<>(Objects.requireNonNull(metrics, "Metrics cannot be null.")));

    this.baseLoad = baseLoad;
    this.variance = variance;
    this.burstChance = burstChance;
  }

  /**
   * Provider-specific service name.
   *
   * <p>Examples: AWS/EC2 AWS/RDS compute.googleapis.com/instance
   */
  public String serviceName() {
    return serviceName;
  }

  /** Base utilisation around which the service fluctuates. */
  public double baseLoad() {
    return baseLoad;
  }

  /** Variance applied when generating the initial mean. */
  public double variance() {
    return variance;
  }

  /** Probability that a burst event occurs. */
  public double burstChance() {
    return burstChance;
  }

  /** Returns a metric by name. */
  public MetricDefinition metric(String metricName) {

    MetricDefinition metric = metrics.get(metricName);

    if (metric == null) {
      throw new IllegalArgumentException(
          "Unknown metric '" + metricName + "' for service " + serviceName);
    }

    return metric;
  }

  /** Returns all metric definitions. */
  public Collection<MetricDefinition> metrics() {
    return metrics.values();
  }

  /** Returns true if this service defines the supplied metric. */
  public boolean containsMetric(String metricName) {
    return metrics.containsKey(metricName);
  }

  /** Number of metrics registered. */
  public int metricCount() {
    return metrics.size();
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof MockServiceDefinition other)) {
      return false;
    }

    return Double.compare(other.baseLoad, baseLoad) == 0
        && Double.compare(other.variance, variance) == 0
        && Double.compare(other.burstChance, burstChance) == 0
        && serviceName.equals(other.serviceName)
        && metrics.equals(other.metrics);
  }

  @Override
  public int hashCode() {

    return Objects.hash(serviceName, baseLoad, variance, burstChance, metrics);
  }

  @Override
  public String toString() {

    return "MockServiceDefinition{"
        + "serviceName='"
        + serviceName
        + '\''
        + ", metrics="
        + metrics.size()
        + '}';
  }
}
