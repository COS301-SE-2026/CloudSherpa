package com.cloudsherpa.ingestion.provider.mock.definition;

import com.cloudsherpa.ingestion.provider.mock.simulation.MetricSimulator;
import java.util.Objects;

public record MetricDefinition(
    String name, String unit, SimulationProfile profile, MetricSimulator simulator) {

  public MetricDefinition(
      String name, String unit, SimulationProfile profile, MetricSimulator simulator) {
    this.name = Objects.requireNonNull(name, "Metric name cannot be null.");
    this.unit = Objects.requireNonNull(unit, "Metric unit cannot be null.");
    this.profile = Objects.requireNonNull(profile, "Simulation profile cannot be null.");
    this.simulator = Objects.requireNonNull(simulator, "Metric simulator cannot be null.");
  }

  @Override
  public int hashCode() {

    return Objects.hash(name, unit, profile, simulator);
  }

  @Override
  public String toString() {

    return "MetricDefinition{" + "name='" + name + '\'' + ", unit='" + unit + '\'' + '}';
  }
}
