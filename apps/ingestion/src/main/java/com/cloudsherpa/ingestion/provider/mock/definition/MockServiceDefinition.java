package com.cloudsherpa.ingestion.provider.mock.definition;

import java.util.Map;

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

    this.serviceName = serviceName;
    this.baseLoad = baseLoad;
    this.variance = variance;
    this.burstChance = burstChance;
    this.metrics = Map.copyOf(metrics);
  }

  public String serviceName() {
    return serviceName;
  }

  public double baseLoad() {
    return baseLoad;
  }

  public double variance() {
    return variance;
  }

  public double burstChance() {
    return burstChance;
  }

  public MetricDefinition metric(String name) {
    return metrics.get(name);
  }

  public Map<String, MetricDefinition> metrics() {
    return metrics;
  }
}
